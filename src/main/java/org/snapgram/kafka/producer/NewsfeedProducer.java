package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.FollowCreatedMessage;
import org.snapgram.dto.kafka.NewsfeedMessage;
import org.snapgram.dto.kafka.PostCreatedMessage;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NewsfeedProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostCreatedMessage(UUID creatorId, UUID postId, Timestamp createdAt) {
        if (creatorId == null || postId == null || createdAt == null) {
            log.error("Invalid input for PostCreatedMessage");
            return;
        }
        PostCreatedMessage post = new PostCreatedMessage(creatorId, postId, createdAt);
        NewsfeedMessage message = NewsfeedMessage.builder()
                .type(NewsfeedMessage.NewsfeedType.POST_CREATED)
                .data(post).build();
        kafkaTemplate.send(KafkaTopicConstant.NEWSFEED_TOPIC, message);
    }

    public void sendFollowCreatedMessage(UUID followerId, UUID followeeId) {
        if (followerId == null || followeeId == null) {
            log.error("Invalid input for FollowCreatedMessage");
            return;
        }
        FollowCreatedMessage follow = new FollowCreatedMessage(followerId, followeeId);
        NewsfeedMessage message = NewsfeedMessage.builder()
                .type(NewsfeedMessage.NewsfeedType.FOLLOW_CREATED)
                .data(follow).build();
        kafkaTemplate.send(KafkaTopicConstant.NEWSFEED_TOPIC, message);
    }

    public void sendUnfollowMessage(UUID userId, UUID followeeId) {
        if (userId == null || followeeId == null) {
            log.error("Invalid input for UnfollowMessage");
            return;
        }
        FollowCreatedMessage follow = new FollowCreatedMessage(userId, followeeId);
        NewsfeedMessage message = NewsfeedMessage.builder()
                .type(NewsfeedMessage.NewsfeedType.UNFOLLOW)
                .data(follow).build();
        kafkaTemplate.send(KafkaTopicConstant.NEWSFEED_TOPIC, message);
    }
}
