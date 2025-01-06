package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        redisService.deleteEntriesFromMap(message.redisKey(), (List<Object>) message.obj());
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_VALUE_TO_REDIS_TOPIC)
    public void saveValueToRedis(SaveRedisMessage message) {
        redisService.saveValue(message.getRedisKey(), message.getObj());
        boolean isNone;
        if (message.getObj() instanceof String string) {
            isNone = StringUtils.isBlank(string);
        } else {
            isNone = message.getObj() == null;
        }
        setTTL(message.getRedisKey(), message.getTimeout(), message.getTimeUnit(), isNone);
    }

    private void setTTL(String key, Long timeout, TimeUnit timeUnit, boolean isNone) {
        if (timeout != null && timeUnit != null) {
            redisService.setTTL(key, timeout, timeUnit);
        } else if (isNone) {
            redisService.setTTL(key, 30L, TimeUnit.SECONDS);
        }
    }

    @KafkaListener(topics = KafkaTopicConstant.DELETE_KEY_REDIS_TOPIC)
    public void deleteRedisByKey(String message) {
        redisService.deleteByPattern(message + "*");
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_LIST_TO_REDIS_TOPIC)
    private void saveListToRedis(SaveRedisMessage message) {
        try {
            if (message.getObj() instanceof List<?> list) {
                redisService.saveList(message.getRedisKey(), list, message.getIndex());
                setTTL(message.getRedisKey(), message.getTimeout(), message.getTimeUnit(), list.isEmpty());
            } else {
                log.error("Invalid data type for list saving in Redis. Expected List but found: {}", message.getObj().getClass());
            }
        } catch (Exception e) {
            log.error("Error saving list to Redis: ", e);
        }
    }

    @KafkaListener(topics = KafkaTopicConstant.SAVE_SET_TO_REDIS_TOPIC)
    private void saveSetToRedis(SaveRedisMessage message) {
        try {
            Set<Object> set = null;

            if (message.getObj() instanceof List<?> list) {
                set = new HashSet<>(list);
            } else if (message.getObj() instanceof Set<?> existingSet) {
                set = new HashSet<>(existingSet);
            } else {
                log.error("Invalid data type for set saving in Redis. Expected List or Set but found: {}",
                        message.getObj().getClass());
                return;
            }

            redisService.saveSet(message.getRedisKey(), set);
            setTTL(message.getRedisKey(), message.getTimeout(), message.getTimeUnit(), set.isEmpty());
        } catch (Exception e) {
            log.error("Error saving set to Redis: ", e);
        }
    }


    @KafkaListener(topics = KafkaTopicConstant.SAVE_MAP_TO_REDIS_TOPIC)
    private void saveMapToRedis(SaveRedisMessage message) {
        try {
            if (!(message.getObj() instanceof Map<?, ?>)) {
                log.error("Invalid data type for map saving in Redis. Expected map but found: {}", message.getObj().getClass());
            }

            Map<Object, Object> map = (Map<Object, Object>) message.getObj();
            redisService.addEntriesToMap(message.getRedisKey(), map);

            setTTL(message.getRedisKey(), message.getTimeout(), message.getTimeUnit(), map.isEmpty());
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
