package org.snapgram.kafka.consumer;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.KafkaTopicConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class NotificationConsumer {
    IRedisService redisService;

    @KafkaListener(topics = KafkaTopicConstant.NOTIFICATION_TOPIC)
    public void handleNotificationMessage(@NotNull String message) {
        log.info("Received notification message for user: {}", message);
        UUID recipientId = UUID.fromString(message);
        redisService.addEntriesToMap(RedisKeyUtil.READ_NOTIFICATION, Map.of(recipientId.toString(), false));
    }
}
