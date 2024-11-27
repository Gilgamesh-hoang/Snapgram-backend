package org.snapgram.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.comment.ICommentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/comments")
@Validated
public class CommentLikeController {
    ICommentService commentService;

    @PostMapping("/liked-by-user/filter")
    public ResponseObject<List<UUID>> filterLiked(@RequestBody @NotNull List<UUID> commentIds,
                                                  @AuthenticationPrincipal CustomUserSecurity user) {
        if (commentIds.isEmpty()) {
            return new ResponseObject<>(HttpStatus.OK, List.of());
        }
        List<UUID> commentLikedIds = commentService.filterLiked(user.getId(), commentIds);
        return new ResponseObject<>(HttpStatus.OK, commentLikedIds);
    }

    @PostMapping("/{commentId}/like")
    public ResponseObject<Integer> likeComment(@PathVariable("commentId") @NotNull UUID commentId) {
        int response = commentService.like(commentId);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

    @DeleteMapping("/{commentId}/unlike")
    public ResponseObject<Integer> unlikeComment(@PathVariable("commentId") @NotNull UUID commentId) {
        int response = commentService.unlike(commentId);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

}
