package org.snapgram.service.comment;

import org.snapgram.dto.request.CommentRequest;
import org.snapgram.dto.request.ReplyCommentRequest;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    List<CommentDTO> getCommentsByPost(UUID postId, Pageable pageable);

    CommentDTO createComment(UUID currentUser, CommentRequest request);

    CommentDTO createComment(UUID id, ReplyCommentRequest request);

    void updateReplyCount(UUID commentId);

    List<CommentDTO> getRepliesByComment(UUID commentId, Pageable pageable);

    CommentDTO editComment(UUID currentUserId, UUID commentId, String content);

    int deleteComment(UUID currentUserId, UUID commentId);

    CommentDTO getCommentById(UUID commentId);

    UUID getPostIdByComment(UUID commentId);

    PostDTO getPostByComment(UUID commentId);
}
