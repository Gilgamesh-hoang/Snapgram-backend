package org.snapgram.socket;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.MessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.enums.ConversationType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.message.MessageService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.snapgram.util.AppConstant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SocketModule {
    SocketIOServer socketServer;
    UserSocketManager userSocketManager;
    JwtHelper jwtHelper;
    IKeyService keyService;
    UserDetailServiceImpl userDetailService;
    JwtService jwtService;
    MessageService messageService;

    @PostConstruct
    public void initializeSocketHandlers() {
        socketServer.addConnectListener(onConnected());
        socketServer.addDisconnectListener(onDisconnected());
        socketServer.addEventListener(AppConstant.SEND_MESSAGE_EVENT, MessageRequest.class, messageService);
    }

    private ConnectListener onConnected() {
        return client -> {
            String jwt = getAuthToken(client.getHandshakeData());
            if (StringUtils.isBlank(jwt)) {
                rejectClient(client, "Invalid or missing token, rejecting connection.");
                return;
            }

            UUID userId = authentication(jwt, client);
            if (userId != null) {
                addUserToConversationRooms(userId, client);

                userSocketManager.addUserSocket(userId, client);

                log.info("User [{}] connected with socket ID [{}]", userId, client.getSessionId());
            } else {
                rejectClient(client, "Authentication failed.");
                return;
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            UUID userId = userSocketManager.removeSocket(client);

            outOfRooms(userId, client);
            if (userId != null) {
                log.info("User [{}] disconnected from socket", userId);
            }
        };
    }

    private void addUserToConversationRooms(UUID userId, SocketIOClient client) {
        List<ConversationDTO> conversations = messageService.getConversationsByType(userId, ConversationType.GROUP);
        for (ConversationDTO conversation : conversations) {
            String roomId = conversation.getId().toString();
            client.joinRoom(roomId);
            log.info("User {} joined room {}", userId, roomId);
        }
    }

    private void outOfRooms(UUID userId, SocketIOClient client) {
        client.getAllRooms().forEach(client::leaveRoom);
        log.info("User {} disconnected and left all rooms", userId);
    }

    private UUID authentication(String jwt, SocketIOClient client) {
        try {
            String email = jwtHelper.extractEmailFromPayload(jwt, false);
            if (StringUtils.isBlank(email)) {
                rejectClient(client, "Email not found in token payload.");
                return null;
            }
            CustomUserSecurity userDetails = (CustomUserSecurity) userDetailService.loadUserByUsername(email);
            String publicKey = keyService.getUserPublicATKey(userDetails.getId());
            // authentication
            if (!jwtService.validateAccessToken(jwt, publicKey)) {
                rejectClient(client, "Token validation failed.");
                return null;
            }

            return userDetails.getId();
        } catch (IllegalArgumentException e) {
            rejectClient(client, "Unable to parse JWT Token: " + e.getMessage());
        } catch (ExpiredJwtException | ResourceNotFoundException e) {
            rejectClient(client, "JWT Token has expired: " + e.getMessage());
        }
        return null;
    }

    //    private String getAuthToken(HandshakeData handshakeData) {
//        Map<String, Object> authData = (Map<String, Object>) handshakeData.getAuthToken();
//        if (authData != null) {
//            String token = (String) authData.get("token");
//            if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
//                return token.substring("Bearer ".length());
//            } else {
//                return null;
//            }
//        }
//        String authorizationHeader = handshakeData.getHttpHeaders().get("Authorization");
//        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
//            return authorizationHeader.substring("Bearer ".length());
//        }
//        return null;
//    }

    private String getAuthToken(HandshakeData handshakeData) {
        String token = handshakeData.getSingleUrlParam("token");

        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            return token.substring("Bearer ".length());
        }
        return null;
    }


    private void rejectClient(SocketIOClient client, String message) {
        log.warn(message);
        client.sendEvent(AppConstant.ERROR_EVENT, message);
        client.disconnect();
    }



}