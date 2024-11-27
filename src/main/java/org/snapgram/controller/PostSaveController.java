package org.snapgram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.post.IPostSaveService;
import org.snapgram.service.post.IPostService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/posts")
@Validated
public class PostSaveController {
    IPostService postService;
    IPostSaveService postSaveService;

    @PostMapping("/check-saves")
    public ResponseObject<List<UUID>> checkUserSaves(@RequestBody List<UUID> postIds,
                                                     @AuthenticationPrincipal CustomUserSecurity user) {
        List<UUID> savedPostIds = postSaveService.getSavedPosts(user.getId(), postIds);
        return new ResponseObject<>(HttpStatus.OK, savedPostIds);
    }


    @GetMapping("/saved")
    public ResponseObject<List<PostDTO>> getSavedPostsByUser(
            @AuthenticationPrincipal CustomUserSecurity user,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("savedAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<PostDTO> response = postSaveService.getSavedPostsByUser(user.getId(), pageable);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

    @PostMapping("/{postId}/save")
    public ResponseObject<Void> savePost(@PathVariable("postId") @NotNull UUID postId) {
        postService.savePost(postId);
        return new ResponseObject<>(HttpStatus.OK);
    }

    @DeleteMapping("/{postId}/unsaved")
    public ResponseObject<Void> unsavedPost(@PathVariable("postId") @NotNull UUID postId) {
        postService.unsavedPost(postId);
        return new ResponseObject<>(HttpStatus.OK);
    }

}
