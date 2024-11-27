package org.snapgram.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.CommentRequest;
import org.snapgram.dto.request.EditCommentRequest;
import org.snapgram.dto.request.ReplyCommentRequest;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.comment.ICommentService;
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
@RequestMapping("${API_PREFIX}/comments")
@Validated
public class CommentController {
    ICommentService commentService;

    @PostMapping
    public ResponseObject<CommentDTO> createComment(@AuthenticationPrincipal CustomUserSecurity currentUser,
                                                    @RequestBody @Valid CommentRequest request) {
        CommentDTO comment = commentService.createComment(currentUser.getId(), request);
        return new ResponseObject<>(HttpStatus.CREATED, comment);
    }

    @PostMapping("/reply")
    public ResponseObject<CommentDTO> replyComment(@AuthenticationPrincipal CustomUserSecurity currentUser,
                                                   @RequestBody @Valid ReplyCommentRequest request) {
        CommentDTO comment = commentService.createComment(currentUser.getId(), request);
        return new ResponseObject<>(HttpStatus.CREATED, comment);
    }

    @PutMapping("/{commentId}")
    public ResponseObject<CommentDTO> editComment(@AuthenticationPrincipal CustomUserSecurity currentUser,
                                                  @PathVariable("commentId") @NotNull UUID commentId,
                                                  @RequestBody @NotNull EditCommentRequest request) {
        CommentDTO comment = commentService.editComment(currentUser.getId(), commentId, request.getContent());
        return new ResponseObject<>(HttpStatus.OK, comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseObject<Integer> deleteComment(@AuthenticationPrincipal CustomUserSecurity currentUser,
                                                 @PathVariable("commentId") @NotNull UUID commentId) {
        int numberCommentDeleted = commentService.deleteComment(currentUser.getId(), commentId);
        return new ResponseObject<>(HttpStatus.OK, numberCommentDeleted);
    }

    @GetMapping("/posts")
    public ResponseObject<List<CommentDTO>> getCommentsByPost(
            @RequestParam("postId") @NotNull UUID postId,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "30") @Min(0) @Max(50) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<CommentDTO> comments = commentService.getCommentsByPost(postId, pageable);
        return new ResponseObject<>(HttpStatus.OK, comments);
    }

    @GetMapping("/comment")
    public ResponseObject<List<CommentDTO>> getRepliesByComment(
            @RequestParam("commentId") @NotNull UUID commentId,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "30") @Min(0) @Max(50) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<CommentDTO> comments = commentService.getRepliesByComment(commentId, pageable);
        return new ResponseObject<>(HttpStatus.OK, comments);
    }
}
