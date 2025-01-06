package org.snapgram.socket;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.entity.database.message.Message;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.stereotype.Component;

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

    @PostConstruct
    public void initializeSocketHandlers() {
        socketServer.addConnectListener(onConnected());
        socketServer.addDisconnectListener(onDisconnected());
        socketServer.addEventListener("send_message", Message.class, onMessageReceived());
    }

    private ConnectListener onConnected() {
        return client -> {
            String jwt = getAuthToken(client.getHandshakeData());
            if (StringUtils.isBlank(jwt)) {
                rejectClient(client, "Invalid or missing token, rejecting connection.");
                return;
            }

            UUID userId = authentication(jwt, client);
            userSocketManager.addUserSocket(userId, client);
            log.info("User [{}] connected with socket ID [{}]", userId, client.getSessionId());
        };
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
        } catch (ExpiredJwtException e) {
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
        client.disconnect();
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            UUID userId = userSocketManager.removeSocket(client);
            if (userId != null) {
                log.info("User [{}] disconnected from socket", userId);
            }
        };
    }

    private DataListener<Message> onMessageReceived() {
        return (senderClient, message, ackSender) -> {
            log.info("Message received: {}", message);
            // Handle message logic here, e.g., broadcast to a room
        };
    }
}