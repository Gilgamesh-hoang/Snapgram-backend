package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.entity.database.Comment;
import org.snapgram.entity.database.Post;
import org.snapgram.mapper.CommentMapper;
import org.snapgram.repository.database.CommentRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {
    CommentRepository commentRepository;
    CommentMapper commentMapper;
    @Override
    public List<CommentDTO> getCommentsByPost(UUID postId, Pageable pageable) {
        Example<Comment> example = Example.of(Comment.builder()
                .post(Post.builder().id(postId).build())
                .isDeleted(false)
                .build());
        List<Comment> all = commentRepository.findAll(example, pageable).getContent();
        return commentMapper.toDTOs(all);
    }


}
