package org.snapgram.repository.database;

import org.snapgram.entity.database.Key;
import org.snapgram.entity.database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface KeyRepository extends JpaRepository<Key, UUID> {
    @Query("SELECT k.publicKeyAT FROM Key k WHERE k.user.id = :userId")
    String findPublicKeyATByUserId(@Param("userId") UUID userId);

    @Query("SELECT k.privateKeyAT FROM Key k WHERE k.user.id = :userId")
    String findPrivateKeyATByUserId(@Param("userId") UUID userId);

    @Query("SELECT k.publicKeyRT FROM Key k WHERE k.user.id = :userId")
    String findPublicKeyRTByUserId(@Param("userId") UUID userId);

    @Query("SELECT k.privateKeyRT FROM Key k WHERE k.user.id = :userId")
    String findPrivateKeyRTByUserId(@Param("userId") UUID userId);

    void deleteByUser(User user);

}
