package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.socket.UserSocketManager;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.service.notification.NotificationQueue;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class NotificationProducer {
    KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationQueue notificationQueue;
    UserSocketManager userSocketManager;

    public void sendNotificationMessage(NotificationDTO notification) {
        log.info("Notification sent to user: {}", notification.getRecipientId());
        notificationQueue.addNotification(notification);
    }

    @Scheduled(fixedRate = 10000)
    public void processQueue() {
        List<NotificationDTO> notifications = notificationQueue.drainQueue();
        if (notifications.isEmpty()) {
            return;
        }

        // group notifications by notification.getRecipientId()
        Map<UUID, List<NotificationDTO>> groupByRecipient = notifications.stream()
                .collect(Collectors.groupingBy(NotificationDTO::getRecipientId));

        groupByRecipient.forEach((recipientId, group) -> {
            if (userSocketManager.containsUser(recipientId)) {
                kafkaTemplate.send(KafkaTopicConstant.NOTIFICATION_TOPIC,
                        group.stream().sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())).toList());
            }
        });

    }
}