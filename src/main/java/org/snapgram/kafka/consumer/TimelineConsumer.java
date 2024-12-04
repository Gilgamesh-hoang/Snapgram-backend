package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.kafka.FollowCreatedMessage;
import org.snapgram.dto.kafka.PostCreatedMessage;
import org.snapgram.service.timeline.ITimelineService;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimelineConsumer {
    ITimelineService timelineService;

    @KafkaListener(topics = KafkaTopicConstant.TIMELINE_POST_CREATE_TOPIC)
    public void handlePostCreatedMessage(PostCreatedMessage mess) {
        if (mess == null) {
            return;
        }
        timelineService.generateTimeline(mess.creator(), mess.postId(), mess.createdAt());
    }

    @KafkaListener(topics = KafkaTopicConstant.TIMELINE_FOLLOW_CREATE_TOPIC)
    public void consumeFollowCreatedMessage(FollowCreatedMessage message) {
        if (message == null) {
            return;
        }
        timelineService.addPostsToTimeline(message.followerId(), message.followeeId());
    }

    @KafkaListener(topics = KafkaTopicConstant.TIMELINE_UNFOLLOW_TOPIC)
    public void consumeUnfollowMessage(FollowCreatedMessage message) {
        if (message == null) {
            return;
        }
        timelineService.removePostsFromTimeline(message.followerId(), message.followeeId());
    }
}
