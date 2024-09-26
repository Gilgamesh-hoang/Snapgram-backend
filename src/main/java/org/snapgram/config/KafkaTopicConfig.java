package org.snapgram.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.snapgram.util.AppConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic saveValueToRedisTopic() {
        return TopicBuilder.name(AppConstant.SAVE_VALUE_TO_REDIS_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic deleteItemsInMapTopic() {
        return TopicBuilder.name(AppConstant.DELETE_MAP_ITEMS_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic removeMediaTopic() {
        return TopicBuilder.name(AppConstant.REMOVE_MEDIA_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic updateCommentCountTopic() {
        return TopicBuilder.name(AppConstant.UPDATE_COMMENT_COUNT_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic deleteKeyRedisTopic() {
        return TopicBuilder.name(AppConstant.DELETE_KEY_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveListRedisTopic() {
        return TopicBuilder.name(AppConstant.SAVE_LIST_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveMapRedisTopic() {
        return TopicBuilder.name(AppConstant.SAVE_MAP_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic saveSetRedisTopic() {
        return TopicBuilder.name(AppConstant.SAVE_SET_TO_REDIS_TOPIC).partitions(1).build();
    }
    @Bean
    public NewTopic emailVerificationTopic() {
        return TopicBuilder.name(AppConstant.EMAIL_VERIFICATION_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic forgotPasswordTopic() {
        return TopicBuilder.name(AppConstant.FORGOT_PASSWORD_TOPIC).partitions(1).build();
    }

    @Bean
    public NewTopic generateKeyPairTopic() {
        return TopicBuilder.name(AppConstant.GENERATE_KEY_PAIR_TOPIC).partitions(1).build();
    }
}

