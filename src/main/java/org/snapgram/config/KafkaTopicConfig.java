package org.snapgram.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic affinityTopic() {
        return TopicBuilder.name(KafkaTopicConstant.AFFINITY_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic newsfeedTopic() {
        return TopicBuilder.name(KafkaTopicConstant.NEWSFEED_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic deleteItemSetTopic() {
        return TopicBuilder.name(KafkaTopicConstant.DELETE_ITEM_IN_SET_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic postLikeUpdateTopic() {
        return TopicBuilder.name(KafkaTopicConstant.POST_LIKE_UPDATE_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic saveValueToRedisTopic() {
        return TopicBuilder.name(KafkaTopicConstant.SAVE_VALUE_TO_REDIS_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic deleteItemsInMapTopic() {
        return TopicBuilder.name(KafkaTopicConstant.DELETE_MAP_ITEMS_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic removeMediaTopic() {
        return TopicBuilder.name(KafkaTopicConstant.REMOVE_MEDIA_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic updateCommentCountTopic() {
        return TopicBuilder.name(KafkaTopicConstant.UPDATE_COMMENT_COUNT_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic deleteKeyRedisTopic() {
        return TopicBuilder.name(KafkaTopicConstant.DELETE_KEY_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveListRedisTopic() {
        return TopicBuilder.name(KafkaTopicConstant.SAVE_LIST_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveMapRedisTopic() {
        return TopicBuilder.name(KafkaTopicConstant.SAVE_MAP_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveSetRedisTopic() {
        return TopicBuilder.name(KafkaTopicConstant.SAVE_SET_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic emailVerificationTopic() {
        return TopicBuilder.name(KafkaTopicConstant.EMAIL_VERIFICATION_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic forgotPasswordTopic() {
        return TopicBuilder.name(KafkaTopicConstant.FORGOT_PASSWORD_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic generateKeyPairTopic() {
        return TopicBuilder.name(KafkaTopicConstant.GENERATE_KEY_PAIR_TOPIC).partitions(1).build();
    }
}

