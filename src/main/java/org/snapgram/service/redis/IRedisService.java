package org.snapgram.service.redis;

import java.util.List;
import java.util.Map;

public interface IRedisService {
    Object getValue(String key);

    void saveValue(String key, Object value);

    Map<Object, Object> getMap(String key);

    void saveMap(String key, Map<String, Object> map);
    void addElementsToMap(String key,Map<String, Object> map) ;
    void deleteElementsFromMap(String key, List<Object> fields);
    <T> T getElementFromMap(String key, String field, Class<T> clazz);
}
