package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.KeyPair;
import org.snapgram.dto.request.KeyPairRequest;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeyPairProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendGenerateKeyPair(UUID userId) {
        sendGenerateKeyPair(userId, null);
    }
    public void sendGenerateKeyPair(UUID userId, KeyPair keyPair) {
        KeyPairRequest keyPairRequest = new KeyPairRequest(userId, keyPair);
        kafkaTemplate.send(KafkaTopicConstant.GENERATE_KEY_PAIR_TOPIC, keyPairRequest);
    }

}
