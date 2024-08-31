package org.snapgram.service.post;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostSaveService {
    boolean isPostSaveByUser(UUID postId, UUID userId);
    void savePost(UUID postId, boolean isSaved);

    List<PostDTO> getSavedPostsByUser(UUID userId, Pageable pageable);
}
