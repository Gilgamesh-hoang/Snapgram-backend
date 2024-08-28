package org.snapgram.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.comment.ICommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/comments")
@Validated
public class CommentController {
    ICommentService commentService;

    @GetMapping("/posts")
    public ResponseObject<List<CommentDTO>> getCommentsByPost(
            @RequestParam("postId") @NotNull UUID postId,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "30") @Min(0) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<CommentDTO> comments = commentService.getCommentsByPost(postId, pageable);
        return new ResponseObject<>(HttpStatus.OK, comments);
    }
}
