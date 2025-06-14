package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.UpdateCommentCountMessage;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRemoveMedia(List<UUID> removeMedia) {
        kafkaTemplate.send(KafkaTopicConstant.REMOVE_MEDIA_TOPIC, removeMedia);
    }

    public void sendUpdateCommentCount(UUID postId, int commentCount) {
        UpdateCommentCountMessage message = new UpdateCommentCountMessage(postId, commentCount);
        kafkaTemplate.send(KafkaTopicConstant.UPDATE_COMMENT_COUNT_TOPIC, message);
    }

}
