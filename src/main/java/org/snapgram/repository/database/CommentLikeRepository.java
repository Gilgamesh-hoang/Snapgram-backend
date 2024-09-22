package org.snapgram.repository.database;

import io.lettuce.core.dynamic.annotation.Param;
import org.snapgram.entity.database.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    @Query("SELECT c.comment.id FROM CommentLike c WHERE c.user.id = :currentUserId AND c.comment.id IN :commentIds")
    List<UUID> findByCommentIdIn(@Param("currentUserId") UUID currentUserId, @Param("commentIds") List<UUID> commentIds);

}
