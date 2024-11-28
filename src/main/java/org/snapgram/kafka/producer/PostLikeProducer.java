package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.kafka.PostLikeUpdateMessage;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostLikeProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUpdateLike(PostLikeUpdateMessage message) {
        kafkaTemplate.send(KafkaTopicConstant.POST_LIKE_UPDATE_TOPIC, message);
        log.info("Sending message: {}", message);
    }

}
