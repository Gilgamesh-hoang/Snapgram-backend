package org.snapgram.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.kafka.FollowCreatedMessage;
import org.snapgram.dto.kafka.NewsfeedMessage;
import org.snapgram.dto.kafka.PostCreatedMessage;
import org.snapgram.service.newsfeed.INewsfeedService;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class NewsfeedConsumer {
    INewsfeedService timelineService;
    ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConstant.NEWSFEED_TOPIC)
    public void handleNewsfeedMessage(@Valid NewsfeedMessage mess) {
        switch (mess.getType()) {
            case POST_CREATED -> handlePostCreatedMessage(mess.getData());
            case FOLLOW_CREATED -> consumeFollowCreatedMessage(mess.getData());
            case UNFOLLOW -> consumeUnfollowMessage(mess.getData());
        }
    }

    private void handlePostCreatedMessage(Object data) {
        PostCreatedMessage post;
        if (data instanceof Map<?, ?>) {
            post = objectMapper.convertValue(data, PostCreatedMessage.class);
        } else {
            post = (PostCreatedMessage) data;

        }
        timelineService.generateNewsfeed(post.creator(), post.postId(), post.createdAt());
    }

    private void consumeFollowCreatedMessage(Object data) {
        FollowCreatedMessage follow;
        if (data instanceof Map<?, ?>) {
            follow = objectMapper.convertValue(data, FollowCreatedMessage.class);
        } else {
            follow = (FollowCreatedMessage) data;
        }
        timelineService.addPostsToNewsfeed(follow.followerId(), follow.followeeId());
    }

    private void consumeUnfollowMessage(Object data) {
        FollowCreatedMessage follow;
        if (data instanceof Map<?, ?>) {
            follow = objectMapper.convertValue(data, FollowCreatedMessage.class);
        } else {
            follow = (FollowCreatedMessage) data;
        }
        timelineService.removePostsInNewsfeed(follow.followerId(), follow.followeeId());
    }
}
