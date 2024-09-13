package org.snapgram.service.post;

import java.util.UUID;

public interface IPostLikeService {
    boolean isPostLikedByUser(UUID postId, UUID userId);

    void like(UUID postId);
    void unlike(UUID postId);

    int countByPost(UUID postId);
}
