package org.snapgram.service.post;

import java.util.UUID;

public interface IPostLikeService {
    boolean isPostLikedByUser(UUID postId, UUID userId);

    boolean likePost(UUID postId, boolean isLiked);
}
