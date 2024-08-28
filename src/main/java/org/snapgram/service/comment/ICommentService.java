package org.snapgram.service.comment;

import org.snapgram.dto.response.CommentDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    List<CommentDTO> getCommentsByPost(UUID postId, Pageable pageable);
}
