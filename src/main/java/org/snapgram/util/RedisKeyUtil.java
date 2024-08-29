package org.snapgram.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class RedisKeyUtil {

    public String getSearchUserKey(String keyword, int page, int size) {
        return String.format("search:user:%s:page:%d:size:%d", keyword, page, size);
    }
    public String getFriendSuggestKey(String email) {
        return String.format("suggest-friends:%s", email);
    }
    public String getUserPostKey(String nickname, int page, int size) {
        return String.format("user:%s:posts:page:%d:size:%d", nickname, page, size);
    }

    public static String getPostKey(UUID id) {
        return String.format("post:%s", id.toString());
    }
}
