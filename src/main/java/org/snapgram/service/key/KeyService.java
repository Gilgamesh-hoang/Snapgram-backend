package org.snapgram.service.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.KeyPair;
import org.snapgram.entity.database.Key;
import org.snapgram.entity.database.User;
import org.snapgram.exception.KeyGenerationException;
import org.snapgram.mapper.KeyMapper;
import org.snapgram.repository.database.KeyRepository;
import org.snapgram.util.TripleDESEncoder;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyService implements IKeyService {
    KeyRepository keyRepository;
    KeyMapper keyMapper;
    TripleDESEncoder encoder;
    AsyncKeyService asyncKeyService;

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
        User user = User.builder().id(userId).build();
        Example<Key> example = Example.of(Key.builder().user(user).build());
        Key key = keyRepository.findOne(example).orElse(null);

        if (key != null) {
            key.setPrivateKeyAT(encoder.decode(key.getPrivateKeyAT()));
            key.setPrivateKeyRT(encoder.decode(key.getPrivateKeyRT()));
            return keyMapper.toDto(key);
        } else {
            // Save the key synchronously before returning it
            KeyPair keyPair = generateKeyPair();
            save(keyPair, userId);
            return keyPair;
        }
    }

    @Override
    public String getUserPublicATKey(UUID userId) {
        return keyRepository.findPublicKeyATByUserId(userId);
    }

    @Override
    public String getUserPublicRTKey(UUID userId) {
        return keyRepository.findPublicKeyRTByUserId(userId);
    }

    @Override
    public String getUserPrivateATKey(UUID userId) {
        return keyRepository.findPrivateKeyATByUserId(userId);
    }

    @Override
    public String getUserPrivateRTKey(UUID userId) {
        return keyRepository.findPrivateKeyRTByUserId(userId);
    }


    @Override
    public void save(KeyPair keyPair, UUID userId) {
        Key key = keyMapper.toEntity(keyPair);
        key.setPrivateKeyAT(encoder.encode(keyPair.getPrivateKeyAT()));
        key.setPrivateKeyRT(encoder.encode(keyPair.getPrivateKeyRT()));
        key.setUser(User.builder().id(userId).build());
        keyRepository.save(key);
    }


    @Override
    public void deleteAndSave(KeyPair keyPair, UUID userId) {
        asyncKeyService.delete(userId);
        save(keyPair, userId);
    }

    @Override
    public void deleteUserKey(UUID userId) {
        asyncKeyService.delete(userId);
    }
}
