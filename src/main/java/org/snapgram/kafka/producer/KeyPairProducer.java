package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.util.AppConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeyPairProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void generateKeyPair(UUID userId) {
        kafkaTemplate.send(AppConstant.GENERATE_KEY_PAIR_TOPIC, userId);
    }

}
