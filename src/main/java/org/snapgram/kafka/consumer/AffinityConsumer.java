package org.snapgram.kafka.consumer;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.AppConstant;
import org.snapgram.util.KafkaTopicConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class AffinityConsumer {
    IRedisService redisService;

    @KafkaListener(topics = KafkaTopicConstant.AFFINITY_TOPIC)
    public void handleUpdateCommentCount(@Valid AffinityDTO message) {
        String field = message.getFollowerId().toString() + AppConstant.AFFINITY_SEPARATOR + message.getFolloweeId().toString();
        redisService.incrementHashValue(RedisKeyUtil.AFFINITY, field, 1);
    }
}
