package org.snapgram.service.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface IRedisService {
    Object getValue(String key);

    void saveValue(String key, Object value);

    Map<Object, Object> getMap(String key);

    void saveMap(String key, Map<String, Object> map);
    <T>  void saveList(String key, List<T> list);
    <T>  List<T>  getList(String key, int start, int end);

    void addElementsToMap(String key, Map<String, Object> map);

    void deleteElementsFromMap(String key, List<Object> fields);

    <T> T getElementFromMap(String key, String field, Class<T> clazz);

    <T> Set<T> getSet(String key);

    <T> void saveSet(String key, Set<T> set);

    void setTimeout(String key, long timeout, TimeUnit timeUnit);
}
