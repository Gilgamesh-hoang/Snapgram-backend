package org.snapgram.repository;

import org.snapgram.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByToken(String token);
}
