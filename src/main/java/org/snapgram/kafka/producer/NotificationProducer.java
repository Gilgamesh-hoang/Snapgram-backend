package org.snapgram.kafka.producer;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.KafkaTopicConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class NotificationProducer {
    KafkaTemplate<String, Object> kafkaTemplate;
    IRedisService redisService;

    public void sendNotificationMessage(@NotNull UUID recipientId) {
        Boolean isRead = redisService.getEntryFromMap(RedisKeyUtil.READ_NOTIFICATION, recipientId, Boolean.class);
        if (isRead != null && !isRead) {
            return;
        }

        log.info("Sending notification message for user: {}", recipientId);
        kafkaTemplate.send(KafkaTopicConstant.NOTIFICATION_TOPIC, recipientId);
    }
}
