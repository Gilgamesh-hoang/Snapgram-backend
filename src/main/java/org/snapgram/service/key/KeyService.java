package org.snapgram.service.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.KeyPair;
import org.snapgram.exception.KeyGenerationException;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.AESEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyService implements IKeyService {
    AESEncoder encoder;
    AsyncKeyService asyncKeyService;
    IRedisService redisService;
    RedisProducer redisProducer;

    @Override
    public KeyPair generateKeyPair() {
        CompletableFuture<String> atFuture = asyncKeyService.generateKeyPairAsync();
        CompletableFuture<String> rtFuture = asyncKeyService.generateKeyPairAsync();

        try {
            // Chờ cả hai quá trình hoàn tất
            CompletableFuture.allOf(atFuture, rtFuture).join();

            // Lấy kết quả sau khi các quá trình hoàn tất
            String atKeys = atFuture.get();
            String rtKeys = rtFuture.get();

            String[] atKeyParts = atKeys.split(";");
            String[] rtKeyParts = rtKeys.split(";");

            return KeyPair.builder()
                    .publicKeyAT(atKeyParts[1])
                    .privateKeyAT(atKeyParts[0])
                    .publicKeyRT(rtKeyParts[1])
                    .privateKeyRT(rtKeyParts[0])
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new KeyGenerationException("Error generating key pairs", e);
        }
    }

    @Override
    public KeyPair getKeyPairByUser(UUID userId) {
        KeyPair key = redisService.getElementFromMap(RedisKeyUtil.ASYM_KEYPAIR, userId.toString(), KeyPair.class);

        if (key != null) {
            CompletableFuture<String> privateAT = encoder.decode(key.getPrivateKeyAT());
            CompletableFuture<String> privateRT = encoder.decode(key.getPrivateKeyRT());
            CompletableFuture.allOf(privateAT, privateRT).join();
            try {
                key.setPrivateKeyAT(privateAT.get());
                key.setPrivateKeyRT(privateRT.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new BadCredentialsException("Error while decoding keys", e);
            }
            return key;
        } else {
            // Save the key synchronously before returning it
            KeyPair keyPair = generateKeyPair();
            save(keyPair, userId);
            return keyPair;
        }
    }

    @Override
    public String getUserPublicATKey(UUID userId) {
        return getKeyPairByUser(userId).getPublicKeyAT();
    }

    @Override
    public String getUserPublicRTKey(UUID userId) {
        return getKeyPairByUser(userId).getPublicKeyRT();
    }


    @Override
    public void save(KeyPair keyPair, UUID userId) {
        asyncKeyService.save(keyPair, userId);
    }

    @Override
    public void deleteAndSave(KeyPair keyPair, UUID userId) {
        deleteUserKey(userId);
        save(keyPair, userId);
    }

    @Override
    public void deleteUserKey(UUID userId) {
        redisProducer.sendDeleteElementsInMap(RedisKeyUtil.ASYM_KEYPAIR, List.of(userId.toString()));
    }
}
