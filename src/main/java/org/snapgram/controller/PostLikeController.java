package org.snapgram.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostMetricDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.post.IPostLikeService;
import org.snapgram.service.post.IPostService;
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
public class PostLikeController {
    IPostService postService;
    IPostLikeService postLikeService;

    @PostMapping("/check-likes")
    public ResponseObject<List<UUID>> checkUserLikes(@RequestBody List<UUID> postIds,
                                                     @AuthenticationPrincipal CustomUserSecurity user) {
        List<UUID> likedPostIds = postLikeService.getLikedPosts(user.getId(), postIds);
        return new ResponseObject<>(HttpStatus.OK, likedPostIds);
    }

    @PostMapping("/{postId}/like")
    public ResponseObject<PostMetricDTO> likePost(@PathVariable("postId") @NotNull UUID postId) {
        PostMetricDTO response = postService.like(postId);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseObject<PostMetricDTO> unlikePost(@PathVariable("postId") @NotNull UUID postId) {
        PostMetricDTO response = postService.unlike(postId);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

}
