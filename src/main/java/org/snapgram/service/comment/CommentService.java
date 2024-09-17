package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.request.CommentRequest;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.entity.database.Comment;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.CommentMapper;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.CommentRepository;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {
    IPostService postService;
    CommentRepository commentRepository;
    CommentMapper commentMapper;
    IUserService userService;
    UserMapper userMapper;
    IRedisService redisService;

    @Override
    public List<CommentDTO> getCommentsByPost(UUID postId, Pageable pageable) {
        String redisKey = RedisKeyUtil.getPostCommentsKey(postId, pageable.getPageNumber(), pageable.getPageSize());
        List<CommentDTO> comments = redisService.getList(redisKey);
        if (comments != null) {
            return comments;
        }
        if (!postService.isExist(postId)) {
            throw new ResourceNotFoundException("Post not found");
        }
        Example<Comment> example = Example.of(Comment.builder()
                .post(Post.builder().id(postId).build())
                .isDeleted(false)
                .build());
        comments = commentMapper.toDTOs(commentRepository.findAll(example, pageable).getContent());

        redisService.saveList(redisKey, comments);
        if (comments.isEmpty()) {
            redisService.setTTL(redisKey, 5, TimeUnit.MINUTES);
        } else {
            redisService.setTTL(redisKey, 1, TimeUnit.DAYS);
        }
        return comments;
    }

    @Override
    public CommentDTO createComment(UUID currentUser, CommentRequest request) {
        UUID postId = request.getPostId();
        if (!postService.isExist(request.getPostId())) {
            throw new ResourceNotFoundException("Post not found");
        }

        Comment comment = Comment.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(currentUser).build())
                .content(request.getContent())
                .likeCount(0)
                .isDeleted(false)
                .build();
        commentRepository.saveAndFlush(comment);

        CompletableFuture.runAsync(() -> {
            Example<Comment> example = Example.of(Comment.builder()
                    .post(Post.builder().id(postId).build())
                    .isDeleted(false)
                    .build());
            postService.updateCommentCount(postId, (int) commentRepository.count(example));
            String redisKey = RedisKeyUtil.getPostCommentsKey(postId, 0, 0);
            redisService.deleteByPrefix(redisKey.substring(0, redisKey.indexOf("page")));
        });
        CommentDTO result = commentMapper.toDTO(comment);
        result.setCreator(userMapper.toCreatorDTO(userService.findById(currentUser)));
        return result;
    }


}
