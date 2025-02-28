package org.snapgram.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class RedisService implements IRedisService {
    RedisTemplate<String, Object> redisTemplate;
    RedissonClient redissonClient;

    @Override
    public void saveValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> T getValue(String key, Class<T> clazz) {
        Object data = redisTemplate.opsForValue().get(key);
        // Convert data from JSON (String) to the desired type

        if (data == null) {
            return null;
        }
        if (data instanceof Map<?, ?>) {
            ObjectMapper objectMapper = new ObjectMapper();
            data = objectMapper.convertValue(data, clazz);
            return (T) data;
        }
        return clazz.cast(data);
    }

    @Override
    public <T> void saveList(String key, List<T> list, Integer index) {
        if (index == null) {
            redisTemplate.opsForList().rightPushAll(key, list.toArray());
        } else {
            for (T item : list) {
                redisTemplate.opsForList().set(key, index, item);
                index++;
            }
        }
    }

    @Override
    public <T> List<T> getList(String key, int start, int end, Class<T> clazz) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }

        long size = redisTemplate.opsForList().size(key);
        if (size == 0) {
            return null;
        }
        if (start >= size) {
            return new ArrayList<>();
        }
        if (end > size) {
            end = (int) size;
        }

        List<?> data = redisTemplate.opsForList().range(key, start, end);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        if (data.get(0) instanceof Map<?, ?>) {
            ObjectMapper objectMapper = new ObjectMapper();
            return data.stream().map(item -> objectMapper.convertValue(item, clazz)).toList();
        }
        return (List<T>) data;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        // check key is exist
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        return getList(key, 0, -1, clazz);
    }


    @Override
    public void addEntriesToMap(String key, Map<Object, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Map<Object, Object> getMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Map<Object, Object> popAllEntriesFromMapWithLock(String key) {
        String lockKey = key + ":lock";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 2, TimeUnit.SECONDS)) {
                try {
                    Map<Object, Object> result = getMap(key);
                    deleteByPattern(key);
                    return result;
                } finally {
                    lock.unlock();
                }
            } else {
                return new HashMap<>();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while waiting for lock", e);
        }
    }

    @Override
    public void incrementHashValue(String key, Object field, long value) {
        String lockKey = key + ":lock";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(10, 2, TimeUnit.SECONDS)) {
                try {
                    redisTemplate.opsForHash().increment(key, field, value);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while waiting for lock", e);
        }
    }


    @Override
    public void deleteEntriesFromMap(String key, List<Object> fields) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (Object field : fields) {
                stringRedisConn.hDel(key, (String) field);
            }
            return null;
        });
    }

    @Override
    public <T> T getEntryFromMap(String key, Object hashKey, Class<T> clazz) {
        hashKey = hashKey.toString();
        Object data = redisTemplate.opsForHash().get(key, hashKey);
        if (data == null) {
            return null;
        }
        // Convert data from JSON (String) to the desired type
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, clazz);
    }


    @Override
    public <T> Set<T> getSet(String key) {
        Set<T> results = new HashSet<>();
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members != null && !members.isEmpty()) {
            members.forEach(result -> {
                HashSet<T> set = (HashSet<T>) result;
                results.addAll(set);
            });
            return results;
        }
        return new HashSet<>();

    }

    @Override
    public boolean containInSet(String key, Object item) {
        return redisTemplate.opsForSet().isMember(key, item);
    }

    @Override
    public <T> void saveSet(String key, Set<T> set) {
        redisTemplate.opsForSet().add(key, set);
    }

    @Override
    public void setTTL(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void deleteItemsFromSet(String key, List<Object> items) {
        redisTemplate.opsForSet().remove(key, items.toArray());
    }

}
