package org.snapgram.service.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.KeyPair;
import org.snapgram.exception.KeyGenerationException;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.AESEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AsyncKeyService {
    IRedisService redisService;
    AESEncoder encoder;

    @Async
    public CompletableFuture<Void> save(KeyPair keyPair, UUID userId) {
        CompletableFuture<String> privateAT = encoder.encode(keyPair.getPrivateKeyAT());
        CompletableFuture<String> privateRT = encoder.encode(keyPair.getPrivateKeyRT());
        CompletableFuture.allOf(privateAT, privateRT).join();
        try {
            KeyPair key = new KeyPair();
            key.setPublicKeyAT(keyPair.getPublicKeyAT());
            key.setPublicKeyRT(keyPair.getPublicKeyRT());
            key.setPrivateKeyAT(privateAT.get());
            key.setPrivateKeyRT(privateRT.get());
            HashMap<String, Object> map = new HashMap<>();
            map.put(userId.toString(), key);
            redisService.addElementsToMap(RedisKeyUtil.ASYM_KEYPAIR, map);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BadCredentialsException("Error while encoding keys", e);
        }
    }

    @Async
    public CompletableFuture<String> generateKeyPairAsync() {
        return CompletableFuture.supplyAsync(() -> {
            KeyPairGenerator keyPairGenerator = null;
            try {
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
            } catch (NoSuchAlgorithmException e) {
                throw new KeyGenerationException("Failed to generate key pair", e);
            }
            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + ";" +
                    Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        });
    }
}
