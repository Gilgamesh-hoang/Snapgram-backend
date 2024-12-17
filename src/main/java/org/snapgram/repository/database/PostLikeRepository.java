package org.snapgram.repository.database;

import io.lettuce.core.dynamic.annotation.Param;
import org.snapgram.entity.database.post.Post;
import org.snapgram.entity.database.post.PostLike;
import org.snapgram.entity.database.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    int deleteByPostAndUser(Post post, User user);

    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :currentUserId AND pl.post.id IN :postIds")
    List<UUID> findLikedPosts(@Param("userId") UUID currentUserId, @Param("postIds") List<UUID> postIds);

}
