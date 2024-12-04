package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.DeleteRedisMessage;
import org.snapgram.dto.kafka.SaveRedisMessage;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisConsumer {
    IRedisService redisService;

    @KafkaListener(topics = KafkaTopicConstant.DELETE_MAP_ITEMS_TOPIC)
    public void deleteElementsRedis(DeleteRedisMessage message) {
        redisService.deleteElementsFromMap(message.redisKey(), (List<Object>) message.obj());
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_VALUE_TO_REDIS_TOPIC)
    public void saveValueToRedis(SaveRedisMessage message) {
        redisService.saveValue(message.redisKey(), message.obj());
        if (message.obj() == null) {
            redisService.setTTL(message.redisKey(), 1, TimeUnit.MINUTES);
        } else {
            redisService.setTTL(message.redisKey(), message.timeout(), message.timeUnit());
        }
    }

    @KafkaListener(topics = KafkaTopicConstant.DELETE_KEY_REDIS_TOPIC)
    public void deleteRedisByKey(String message) {
        redisService.deleteByPattern(message + "*");
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_LIST_TO_REDIS_TOPIC)
    public void saveListToRedis(SaveRedisMessage message) {
        try {
            if (message.obj() instanceof List<?> list) {
                redisService.saveList(message.redisKey(), list);
                if (list.isEmpty()) {
                    redisService.setTTL(message.redisKey(), 1, TimeUnit.MINUTES);
                } else {
                    redisService.setTTL(message.redisKey(), message.timeout(), message.timeUnit());
                }
            } else {
                log.error("Invalid data type for list saving in Redis. Expected List but found: {}", message.obj().getClass());
            }
        } catch (Exception e) {
            log.error("Error saving list to Redis: ", e);
        }
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_SET_TO_REDIS_TOPIC)
    public void saveSetToRedis(SaveRedisMessage message) {
        try {
            if (message.obj() instanceof List<?> list) {
                Set<Object> set = new HashSet<>(list);
                redisService.saveSet(message.redisKey(), set);
                if (set.isEmpty()) {
                    redisService.setTTL(message.redisKey(), 1, TimeUnit.MINUTES);
                } else {
                    redisService.setTTL(message.redisKey(), message.timeout(), message.timeUnit());
                }
            } else if (message.obj() instanceof Set<?> set) {
                redisService.saveSet(message.redisKey(), set);
                if (set.isEmpty()) {
                    redisService.setTTL(message.redisKey(), 1, TimeUnit.MINUTES);
                } else {
                    redisService.setTTL(message.redisKey(), message.timeout(), message.timeUnit());
                }
            } else {
                log.error("Invalid data type for set saving in Redis. Expected Set but found: {}", message.obj().getClass());
            }
        } catch (Exception e) {
            log.error("Error saving set to Redis: ", e);
        }
    }


    @KafkaListener(topics = KafkaTopicConstant.SAVE_MAP_TO_REDIS_TOPIC)
    public void saveMapToRedis(SaveRedisMessage message) {
        try {
            if (!(message.obj() instanceof Map<?, ?>)) {
                log.error("Invalid data type for map saving in Redis. Expected map but found: {}", message.obj().getClass());
            }

            Map<Object, Object> map = (Map<Object, Object>) message.obj();
            redisService.addElementsToMap(message.redisKey(), map);

            if (message.timeout() == null || message.timeUnit() == null) {
                return;
            }

            if (map.isEmpty()) {
                redisService.setTTL(message.redisKey(), 1, TimeUnit.MINUTES);
            } else {
                redisService.setTTL(message.redisKey(), message.timeout(), message.timeUnit());
            }
        } catch (Exception e) {
            log.error("Error saving list to Redis: ", e);
        }
    }

    @KafkaListener(topics = KafkaTopicConstant.DELETE_ITEM_IN_SET_TOPIC)
    public void deleteItemInSet(DeleteRedisMessage message) {
        if (!(message.obj() instanceof List<?>)) {
            log.error("Invalid data type for set saving in Redis. Expected List but found: {}", message.obj().getClass());
        }
        redisService.deleteItemsFromSet(message.redisKey(), (List<Object>) message.obj());
    }

}
