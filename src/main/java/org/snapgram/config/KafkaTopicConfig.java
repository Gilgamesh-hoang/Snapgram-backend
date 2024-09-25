package org.snapgram.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.snapgram.util.AppConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

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

