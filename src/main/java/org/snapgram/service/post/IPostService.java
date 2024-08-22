package org.snapgram.service.post;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPostService {
    CompletableFuture<PostDTO> createPost(String caption, MultipartFile[] media, List<String> tags);

    int countByUser(UUID userId);

    List<PostDTO> getPostsByUser(String nickname, Pageable pageable);
}
