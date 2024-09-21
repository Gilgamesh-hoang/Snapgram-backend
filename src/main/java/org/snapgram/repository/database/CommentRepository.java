package org.snapgram.repository.database;

import org.snapgram.entity.database.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false")
    List<Comment> findByParentCommentId(UUID parentId);
}
