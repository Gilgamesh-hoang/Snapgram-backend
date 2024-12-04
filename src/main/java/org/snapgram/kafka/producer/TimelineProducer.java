package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.FollowCreatedMessage;
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
public class TimelineProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostCreatedMessage(UUID creatorId, UUID postId, Timestamp createdAt) {
        if (creatorId == null || postId == null || createdAt == null) {
            log.error("Invalid input for PostCreatedMessage");
            return;
        }
        PostCreatedMessage message = new PostCreatedMessage(creatorId, postId, createdAt);
        kafkaTemplate.send(KafkaTopicConstant.TIMELINE_POST_CREATE_TOPIC, message);
    }

    public void sendFollowCreatedMessage(UUID followerId, UUID followeeId) {
        if (followerId == null || followeeId == null) {
            log.error("Invalid input for FollowCreatedMessage");
            return;
        }
        FollowCreatedMessage message = new FollowCreatedMessage(followerId, followeeId);
        kafkaTemplate.send(KafkaTopicConstant.TIMELINE_FOLLOW_CREATE_TOPIC, message);
    }

    public void sendUnfollowMessage(UUID userId, UUID followeeId) {
        if (userId == null || followeeId == null) {
            log.error("Invalid input for UnfollowMessage");
            return;
        }
        FollowCreatedMessage message = new FollowCreatedMessage(userId, followeeId);
        kafkaTemplate.send(KafkaTopicConstant.TIMELINE_UNFOLLOW_TOPIC, message);
    }
}
