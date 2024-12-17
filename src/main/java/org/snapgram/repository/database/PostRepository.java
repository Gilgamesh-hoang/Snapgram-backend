package org.snapgram.repository.database;

import org.snapgram.entity.database.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = :commentCount WHERE p.id = :postId")
    int updateCommentCount(@Param("postId") UUID postId, @Param("commentCount") int commentCount);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.id = :postId")
    int updateLikeCount(@Param("postId") UUID postId, @Param("likeCount") int likeCount);

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.createdAt > :time ORDER BY p.createdAt DESC")
    List<Post> findAllByUserIdsAndAfter(@Param("userIds") List<UUID> userIds, @Param("time") Timestamp time);
}
