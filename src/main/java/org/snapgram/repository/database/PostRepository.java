package org.snapgram.repository.database;

import org.snapgram.entity.database.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = :commentCount WHERE p.id = :postId")
    int updateCommentCount(@Param("postId") UUID postId, @Param("commentCount") int commentCount);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.id = :postId")
    int updateLikeCount(@Param("postId") UUID postId, @Param("likeCount") int likeCount);
}
