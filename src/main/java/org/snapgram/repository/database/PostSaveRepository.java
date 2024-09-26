package org.snapgram.repository.database;

import io.lettuce.core.dynamic.annotation.Param;
import org.snapgram.entity.database.Saved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostSaveRepository extends JpaRepository<Saved, UUID> {
    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    @Query("SELECT s.post.id FROM Saved s WHERE s.user.id = :userId AND s.post.id IN :postIds")
    List<UUID> findSavedPosts(@Param("userId") UUID currentUserId, @Param("postIds") List<UUID> postIds);
}
