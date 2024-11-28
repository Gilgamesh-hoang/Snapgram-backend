package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.PostLikeUpdateMessage;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.KafkaTopicConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostLikeConsumer {
    IRedisService redisService;

    @KafkaListener(topics = KafkaTopicConstant.POST_LIKE_UPDATE_TOPIC)
    public void handleUpdateCommentCount(PostLikeUpdateMessage message) {
        log.info("Received message: {}", message);
        switch (message.getAction()) {
            case INCREMENT:
                redisService.incrementHashValue(RedisKeyUtil.POST_LIKE_COUNT, message.getPostId().toString(), 1);
                break;
            case DECREMENT:
                redisService.incrementHashValue(RedisKeyUtil.POST_LIKE_COUNT, message.getPostId().toString(), -1);
                break;
            default:
                log.error("Invalid message type: {}", message.getAction());
        }

    }
}
