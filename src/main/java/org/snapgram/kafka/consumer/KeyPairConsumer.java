package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.KeyPair;
import org.snapgram.dto.request.KeyPairRequest;
import org.snapgram.service.key.IKeyService;
import org.snapgram.util.AppConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyPairConsumer {
    IKeyService keyService;

    @KafkaListener(topics = AppConstant.GENERATE_KEY_PAIR_TOPIC)
    public void handleGenerateKeyPair(KeyPairRequest keyPairRequest) {
        UUID userId = keyPairRequest.getUserId();
        KeyPair keyPair = keyPairRequest.getKeyPair();
        if (keyPair == null) {
            keyPair = keyService.generateKeyPair();
        }
        keyService.save(keyPair, userId);
    }

}
