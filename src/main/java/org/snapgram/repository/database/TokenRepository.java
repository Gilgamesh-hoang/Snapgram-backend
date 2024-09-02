package org.snapgram.repository.database;

import org.snapgram.entity.database.Token;
import org.snapgram.entity.database.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    List<Token> findByUser(User user);
    Optional<Token> findByRefreshTokenId(UUID refreshTokenId);
    void deleteAllByRefreshTokenIdIn(List<UUID> refreshTokenIds);
    void deleteByRefreshTokenId(UUID refreshTokenIds);
}
