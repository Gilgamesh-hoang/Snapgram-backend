package org.snapgram.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisKeyUtil {

    public String getSearchUserKey(String keyword, int page, int size) {
        return String.format("search:user:%s:page:%d:size:%d", keyword, page, size);
    }
}
