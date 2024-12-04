package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.UpdateCommentCountMessage;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.post.PostMediaService;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostConsumer {
    IPostService postService;
    PostMediaService postMediaService;

    @KafkaListener(topics = KafkaTopicConstant.REMOVE_MEDIA_TOPIC)
    public void handleRemoveMedia(List<UUID> removeMedia) {
        postMediaService.removeMedia(removeMedia);
    }
    @KafkaListener(topics = KafkaTopicConstant.UPDATE_COMMENT_COUNT_TOPIC)
    public void handleUpdateCommentCount(UpdateCommentCountMessage message) {
        postService.updateCommentCount(message.postId(), message.commentCount());
    }
}
