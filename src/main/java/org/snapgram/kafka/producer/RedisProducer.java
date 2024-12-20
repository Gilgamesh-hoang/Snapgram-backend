package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.DeleteRedisMessage;
import org.snapgram.dto.kafka.SaveRedisMessage;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendDeleteElementsInMap(String redisKey, List<T> obj) {
        DeleteRedisMessage message = new DeleteRedisMessage(redisKey, obj);
        kafkaTemplate.send(KafkaTopicConstant.DELETE_MAP_ITEMS_TOPIC, message);
    }

    public <T> void sendSaveValue(String redisKey, T obj, Long timeout, TimeUnit timeUnit) {
        SaveRedisMessage message = new SaveRedisMessage(redisKey, obj, timeout, timeUnit);
        kafkaTemplate.send(KafkaTopicConstant.SAVE_VALUE_TO_REDIS_TOPIC, message);
    }

    public <T> void sendSaveList(String redisKey, List<T> obj, Long timeout, TimeUnit timeUnit) {
        SaveRedisMessage message = new SaveRedisMessage(redisKey, obj, timeout, timeUnit);
        kafkaTemplate.send(KafkaTopicConstant.SAVE_LIST_TO_REDIS_TOPIC, message);
    }
    public <T> void sendSaveList(String redisKey, List<T> obj, Long timeout, TimeUnit timeUnit, Integer index) {
        SaveRedisMessage message = new SaveRedisMessage(redisKey, obj, timeout, timeUnit, index);
        kafkaTemplate.send(KafkaTopicConstant.SAVE_LIST_TO_REDIS_TOPIC, message);
    }
    public <T> void sendSaveSet(String redisKey, Set<T> obj, Long timeout, TimeUnit timeUnit) {
        SaveRedisMessage message = new SaveRedisMessage(redisKey, obj, timeout, timeUnit);
        kafkaTemplate.send(KafkaTopicConstant.SAVE_SET_TO_REDIS_TOPIC, message);
    }
    public <K, V> void sendSaveMap(String redisKey, Map<K, V> obj, Long timeout, TimeUnit timeUnit) {
        SaveRedisMessage message = new SaveRedisMessage(redisKey, obj, timeout, timeUnit);
        kafkaTemplate.send(KafkaTopicConstant.SAVE_MAP_TO_REDIS_TOPIC, message);
    }
    public <K, V> void sendSaveMap(String redisKey, Map<K, V> obj) {
        sendSaveMap(redisKey, obj, null, null);
    }

    public void sendDeleteByKey(String redisKey) {
        kafkaTemplate.send(KafkaTopicConstant.DELETE_KEY_REDIS_TOPIC, redisKey);
    }

    public void sendDeleteItemsInSet(String key, List<UUID> ids) {
        kafkaTemplate.send(KafkaTopicConstant.DELETE_ITEM_IN_SET_TOPIC, new DeleteRedisMessage(key, ids));
    }
}
