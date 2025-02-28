package org.snapgram.kafka.consumer;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.socket.UserSocketManager;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.KafkaTopicConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class NotificationConsumer {
    IRedisService redisService;
    UserSocketManager userSocketManager;
    ObjectMapper objectMapper;


    @KafkaListener(topics = KafkaTopicConstant.NOTIFICATION_TOPIC)
    public void handleNotificationMessage(List<Map<String, Object>> message) throws JsonProcessingException {
        List<NotificationDTO> notifications = message.stream()
                .map(notification -> objectMapper.convertValue(notification, NotificationDTO.class))
                .toList();

        if (notifications.isEmpty()) {
            return;
        }

        UUID recipientId = notifications.get(0).getRecipientId();
        redisService.addEntriesToMap(RedisKeyUtil.READ_NOTIFICATION, Map.of(recipientId.toString(), false));

        List<SocketIOClient> sockets = userSocketManager.getUserSockets(recipientId);
        if (sockets.isEmpty()) {
            return;
        }
        // Convert notification to JSON string
        String notificationMessage = objectMapper.writeValueAsString(notifications);
        sockets.forEach(socket -> socket.sendEvent("notification", notificationMessage));
    }
}
