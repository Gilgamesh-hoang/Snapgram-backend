package org.snapgram.service.post;

import java.util.UUID;

public interface IPostSaveService {
    boolean isPostSaveByUser(UUID postId, UUID userId);
    void savePost(UUID postId, boolean isSaved);
}
