package org.snapgram.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements IRedisService {
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void saveValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    @Override
    public <T> T getValue(String key, Class<T> clazz) {
        Object data = redisTemplate.opsForValue().get(key);
        // Convert data from JSON (String) to the desired type
        return clazz.cast(data);
    }
    @Override
    public void saveMap(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public <T> void saveList(String key, List<T> list) {
        for (T item : list) {
            redisTemplate.opsForList().rightPush(key, item);
        }
    }

    @Override
    public <T> List<T> getList(String key, int start, int end) {
        long size = redisTemplate.opsForList().size(key);
        if (size == 0) {
            return null;
        }
        if (start >= size ) {
            throw new IllegalArgumentException("Start index is greater than the size of the list");
        }
        if (end > size) {
            end = (int) size;
        }
        return (List<T>) redisTemplate.opsForList().range(key, start, end);
    }
    @Override
    public <T> List<T> getList(String key) {
        return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
    }

    public Map<Object, Object> getMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }
    @Override
    public void addElementsToMap(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }


    @Override
    public void deleteElementsFromMap(String key, List<Object> fields) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (Object field : fields) {
                stringRedisConn.hDel(key, (String) field);
            }
            return null;
        });
    }

    @Override
    public <T> T getElementFromMap(String key, String field, Class<T> clazz) {
        Object data = redisTemplate.opsForHash().get(key, field);
        // Convert data from JSON (String) to the desired type
        return clazz.cast(data);
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
    public void setTimeout(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    @Override
    public <T> void saveSet(String key, Set<T> set) {
        redisTemplate.opsForSet().add(key, set);
    }
}
