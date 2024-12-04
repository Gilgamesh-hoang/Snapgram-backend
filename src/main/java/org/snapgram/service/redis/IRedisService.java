package org.snapgram.service.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface IRedisService {
    <T> T getValue(String key, Class<T> clazz);

    void saveValue(String key, Object value);

    Map<Object, Object> getMap(String key);

    void addElementsToMap(String key, Map<Object, Object> map);

    Map<Object, Object> popAllElementsFromMapWithLock(String key);

    void incrementHashValue(String key, Object field, long value);

    void deleteElementsFromMap(String key, List<Object> fields);

    <T> T getElementFromMap(String key, Object hashKey, Class<T> clazz);

    <T> void saveList(String key, List<T> list);

    <T> List<T> getList(String key, int start, int end, Class<T> clazz);

    <T> List<T> getList(String key, Class<T> clazz);

    <T> Set<T> getSet(String key);

    boolean containInSet(String key, Object item);

    <T> void saveSet(String key, Set<T> set);

    void setTTL(String key, long timeout, TimeUnit timeUnit);

    void deleteByPattern(String pattern);


    void deleteItemsFromSet(String key, List<Object> items);
}
