package org.snapgram.service.post;

import org.snapgram.dto.response.PostMetricDTO;

import java.util.List;
import java.util.UUID;

public interface IPostLikeService {
    boolean isPostLikedByUser(UUID postId, UUID userId);

    PostMetricDTO like(UUID postId);

    PostMetricDTO unlike(UUID postId);

    int countByPost(UUID postId);

    List<UUID> getLikedPosts(UUID currentUserId, List<UUID> postIds);
}
