package org.snapgram.service.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.database.User;
import org.snapgram.exception.KeyGenerationException;
import org.snapgram.repository.database.KeyRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AsyncKeyService {
    KeyRepository keyRepository;

    @Transactional
    public void delete(UUID userId) {
        keyRepository.deleteByUser(User.builder().id(userId).build());
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
