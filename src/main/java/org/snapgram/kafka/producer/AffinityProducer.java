package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AffinityProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAffinityMessage(UUID followerId, UUID followeeId) {
        if (followerId == null || followeeId == null || followerId.equals(followeeId)) {
            log.error("Invalid affinity message");
            return;
        }
        AffinityDTO message = AffinityDTO.builder().followerId(followerId).followeeId(followeeId).build();
        kafkaTemplate.send(KafkaTopicConstant.AFFINITY_TOPIC, message);
    }
}
