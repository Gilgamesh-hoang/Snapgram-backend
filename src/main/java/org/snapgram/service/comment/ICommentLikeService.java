package org.snapgram.service.comment;

import java.util.List;
import java.util.UUID;

public interface ICommentLikeService {
    boolean isCommentLikedByUser(UUID commentId, UUID userId);

    int like(UUID commentId);

    int unlike(UUID commentId);

    int countByComment(UUID commentId);

    List<UUID> filterLiked(UUID currentUserId, List<UUID> commentIds);
}
