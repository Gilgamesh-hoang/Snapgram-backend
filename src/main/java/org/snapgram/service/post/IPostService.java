package org.snapgram.service.post;

import org.snapgram.dto.request.PostRequest;
import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPostService {
    CompletableFuture<PostDTO> createPost(PostRequest request, MultipartFile[] media);

    int countByUser(UUID userId);

    List<PostDTO> getPostsByUser(String nickname, Pageable pageable);

    PostDTO getPostById(UUID id);


    PostDTO updatePost(PostRequest request, MultipartFile[] media);

    void savePost(UUID postId, Boolean isSaved);
}
