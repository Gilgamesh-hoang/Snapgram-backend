package org.snapgram.service.post;

import java.util.List;
import java.util.UUID;

public interface IPostLikeService {
    boolean isPostLikedByUser(UUID postId, UUID userId);

    void like(UUID postId);

    void unlike(UUID postId);

    int countByPost(UUID postId);

    List<UUID> getLikedPosts(UUID currentUserId, List<UUID> postIds);
}
