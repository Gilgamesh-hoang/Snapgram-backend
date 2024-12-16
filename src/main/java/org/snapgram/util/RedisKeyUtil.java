package org.snapgram.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class RedisKeyUtil {
    public final String JWT_BLACKLIST = "jwt_blacklist";
    public final String ASYM_KEYPAIR = "asymmetric:keypair:users";
    public final String POST_LIKE_COUNT = "post:likes";
    public final String LAST_REFRESH_TOKEN = "auth:refresh_token_last_used";
    public final String USERS_INACTIVE = "users:inactive";
    public final String GET_TIMELINE_LATEST = "timeline:users:latest";
    public final String AFFINITY = "affinity";

    /*timeline*/
    public String getTimelineKey(UUID userId, int page, int size) {
        return String.format("timeline:%s:page:%d:size:%d", userId.toString(), page, size);
    }

    /*Comment key*/
    public String getPostCommentsKey(UUID postId, int page, int size) {
        return String.format("post:%s:comments:page:%d:size:%d", postId.toString(), page, size);
    }

    /* User key*/
    public String getSearchUserKey(String keyword, int page, int size) {
        return String.format("search:user:%s:page:%d:size:%d", keyword, page, size);
    }

    public String getFriendSuggestKey(UUID userId) {
        return String.format("suggest-friends:%s", userId.toString());
    }

    public String getUserPostKey(String nickname, int page, int size) {
        return String.format("user:%s:posts:page:%d:size:%d", nickname, page, size);
    }

    /*Follow key*/
    public String getUserFollowersKey(UUID userId, int page, int size) {
        return String.format("followers:user:%s:page:%d:size:%d", userId.toString(), page, size);
    }

    public String getUserFollowingKey(UUID userId, int page, int size) {
        return String.format("following:user:%s:page:%d:size:%d", userId.toString(), page, size);
    }

    public static String getSearchFollowersKey(UUID userId, String keyword, int pageNumber, int pageSize) {
        return String.format("search:followers:user:%s:keyword:%s:page:%d:size:%d", userId.toString(), keyword, pageNumber, pageSize);
    }

    public static String getSearchFollowingKey(UUID userId, String keyword, int pageNumber, int pageSize) {
        return String.format("search:following:user:%s:keyword:%s:page:%d:size:%d", userId.toString(), keyword, pageNumber, pageSize);
    }

    /*Post key*/
    public static String getPostKey(UUID id) {
        return String.format("post:%s", id.toString());
    }

    public static String getSavedPostsKey(UUID userId, int pageNumber, int pageSize) {
        return String.format("saved-posts:user:%s:page:%d:size:%d", userId.toString(), pageNumber,pageSize);
    }

}
