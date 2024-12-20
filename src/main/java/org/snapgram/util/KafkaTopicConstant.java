package org.snapgram.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaTopicConstant {
    public final String NEWSFEED_TOPIC = "newsfeed";
    public final String EMAIL_VERIFICATION_TOPIC = "email-verification";
    public final String POST_LIKE_UPDATE_TOPIC = "post-like-update";
    public final String FORGOT_PASSWORD_TOPIC = "forgot-password";
    public final String GENERATE_KEY_PAIR_TOPIC = "generate-key-pair";
    public final String SAVE_VALUE_TO_REDIS_TOPIC = "save-value-redis";
    public final String SAVE_LIST_TO_REDIS_TOPIC = "save-list-redis";
    public final String SAVE_SET_TO_REDIS_TOPIC = "save-set-redis";
    public final String SAVE_MAP_TO_REDIS_TOPIC = "save-map-redis";
    public final String DELETE_KEY_REDIS_TOPIC = "delete-key-redis";
    public final String UPDATE_COMMENT_COUNT_TOPIC = "update-comment-count";
    public final String REMOVE_MEDIA_TOPIC = "remove-media";
    public final String DELETE_MAP_ITEMS_TOPIC = "delete-map-items";
    public final String DELETE_ITEM_IN_SET_TOPIC = "delete-item-in-set";
    public final String AFFINITY_TOPIC = "affinity";
    public final String NOTIFICATION_TOPIC = "notification";
}
