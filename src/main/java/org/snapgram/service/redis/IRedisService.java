package org.snapgram.service.redis;

import java.util.Map;

public interface IRedisService {
    Object getValue(String key);

    void saveValue(String key, Object value);

    Map<Object, Object> getMap(String key);

    void saveMap(String key, Map<String, Object> map);
    void addElementToMap(String key, String field, Object value);
    <T> T getElementFromMap(String key, String field, Class<T> clazz);
}
