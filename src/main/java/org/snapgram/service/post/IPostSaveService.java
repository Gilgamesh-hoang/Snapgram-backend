package org.snapgram.service.post;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostSaveService {
    boolean isPostSaveByUser(UUID postId, UUID userId);
    boolean isPostSavedByUser(UUID postId, UUID id);
    List<PostDTO> getSavedPostsByUser(UUID userId, Pageable pageable);

    void savePost(UUID postId);

    void unsavedPost(UUID postId);

    List<UUID> getSavedPosts(UUID currentUserId, List<UUID> postIds);
}
