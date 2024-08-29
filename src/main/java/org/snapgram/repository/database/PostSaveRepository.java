package org.snapgram.repository.database;

import org.snapgram.entity.database.Saved;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostSaveRepository extends JpaRepository<Saved, UUID> {
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
}
