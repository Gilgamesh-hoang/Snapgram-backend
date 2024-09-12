package org.snapgram.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class RedisKeyUtil {

    public String getBlacklistKey() {
        return "jwt_blacklist";
    }
    public String getUserKeyPairHashKey() {
        return "asymmetric:keypair:users";
    }

    public String getSearchUserKey(String keyword, int page, int size) {
        return String.format("search:user:%s:page:%d:size:%d", keyword, page, size);
    }
    public String getFriendSuggestKey(UUID userId) {
        return String.format("suggest-friends:%s", userId.toString());
    }
    public String getUserPostKey(String nickname, int page, int size) {
        return String.format("user:%s:posts:page:%d:size:%d", nickname, page, size);
    }
    public String getUserFollowersKey(UUID userId, int page, int size) {
        return String.format("followers:user:%s:page:%d:size:%d", userId.toString(), page, size);
    }
    public String getUserFollowingKey(UUID userId, int page, int size) {
        return String.format("following:user:%s:page:%d:size:%d", userId.toString(), page, size);
    }

    public static String getPostKey(UUID id) {
        return String.format("post:%s", id.toString());
    }

    public static String getSearchFollowersKey(UUID userId, String keyword, int pageNumber, int pageSize) {
        return String.format("search:followers:user:%s:keyword:%s:page:%d:size:%d", userId.toString(), keyword, pageNumber, pageSize);
    }

    public static String getSearchFollowingKey(UUID userId, String keyword, int pageNumber, int pageSize) {
        return String.format("search:following:user:%s:keyword:%s:page:%d:size:%d", userId.toString(), keyword, pageNumber, pageSize);
    }
}
