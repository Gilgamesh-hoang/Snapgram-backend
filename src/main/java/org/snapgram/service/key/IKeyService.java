package org.snapgram.service.key;

import org.snapgram.dto.KeyPair;

import java.util.UUID;

public interface IKeyService {
    KeyPair generateKeyPair();
    KeyPair getKeyPairByUser(UUID userId);

    String getUserPublicATKey(UUID userId);

    String getUserPublicRTKey(UUID userId);

    String getUserPrivateATKey(UUID userId);

    String getUserPrivateRTKey(UUID userId);

    void save(KeyPair keyPair, UUID userId);
    void deleteAndSave(KeyPair keyPair,UUID userId);

    void deleteUserKey(UUID userId);
}
