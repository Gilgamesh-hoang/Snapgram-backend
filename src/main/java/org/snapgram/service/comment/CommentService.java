package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.request.CommentRequest;
import org.snapgram.dto.request.ReplyCommentRequest;
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
        validatePostExists(postId);

        Example<Comment> example = Example.of(Comment.builder()
                .post(Post.builder().id(postId).build())
                .level(0)
                .isDeleted(false)
                .build());
        comments = fetchCommentsFromDatabase(example, pageable);

        cacheComments(redisKey, comments);
        return comments;
    }

    @Override
    public List<CommentDTO> getRepliesByComment(UUID commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found");
        }
        Example<Comment> example = Example.of(Comment.builder()
                .parentComment(Comment.builder().id(commentId).build())
                .level(1)
                .isDeleted(false)
                .build());
        return fetchCommentsFromDatabase(example, pageable);
    }

    @Override
    public CommentDTO editComment(UUID currentUserId, UUID commentId, String content) {
        Example<Comment> example = Example.of(Comment.builder()
                .id(commentId)
                .user(User.builder().id(currentUserId).build())
                .isDeleted(false)
                .build());
        // Find the comment in the repository, throw an exception if not found
        Comment comment = commentRepository.findOne(example).orElseThrow(
                () -> new ResourceNotFoundException("Comment not found"));
        // Set the new content of the comment
        comment.setContent(content);
        commentRepository.save(comment);

        // Create a Redis key for the post comments
        String redisKey = RedisKeyUtil.getPostCommentsKey(comment.getPost().getId(), 0, 0);
        // Delete the Redis cache for the post comments
        redisService.deleteByPrefix(redisKey.substring(0, redisKey.indexOf("page")));

        return commentMapper.toDTO(comment);
    }

    @Override
    public int deleteComment(UUID currentUserId, UUID commentId) {
        Example<Comment> example = Example.of(Comment.builder()
                .id(commentId)
                .isDeleted(false)
                .build());
        // Find the comment in the repository, throw an exception if not found
        Comment comment = commentRepository.findOne(example).orElseThrow(
                () -> new ResourceNotFoundException("Comment not found"));

        // Get the ID of the user who created the comment
        UUID creatorCommentId = comment.getUser().getId();
        Post post = comment.getPost();

        // Check if the current user is the creator of the comment or the post, throw an exception if not
        if (!currentUserId.equals(creatorCommentId) && !currentUserId.equals(post.getUser().getId())) {
            throw new IllegalArgumentException("You are not allowed to delete this comment");
        }
        // Mark the comment as deleted
        comment.setIsDeleted(true);
        commentRepository.save(comment);

        // Find all child comments of the comment
        List<Comment> commentChild = commentRepository.findByParentCommentId(commentId);
        // Mark all child comments as deleted
        commentChild.forEach(c -> c.setIsDeleted(true));
        commentRepository.saveAll(commentChild);

        // Handle asynchronous comment creation for the post and the parent comment
        handleAsyncCommentCreation(post.getId(), comment.getParentComment() != null ? comment.getParentComment().getId() : null);

        // Return the total number of deleted comments (the comment itself plus all its child comments)
        return commentChild.size() + 1;
    }

    @Override
    public void updateReplyCount(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        Example<Comment> example = Example.of(Comment.builder()
                .parentComment(Comment.builder().id(commentId).build())
                .level(1)
                .isDeleted(false)
                .build());
        comment.setReplyCount((int) commentRepository.count(example));
        commentRepository.saveAndFlush(comment);
    }

    @Override
    public CommentDTO createComment(UUID currentUser, CommentRequest request) {
        UUID postId = request.getPostId();

        validatePostExists(postId);

        Comment comment = buildComment(postId, currentUser, request.getContent(), 0, null);
        commentRepository.saveAndFlush(comment);

        CompletableFuture.runAsync(() -> handleAsyncCommentCreation(postId, null));

        return buildCommentResponse(comment, currentUser);
    }

    @Override
    public CommentDTO createComment(UUID currentUserId, ReplyCommentRequest request) {
        Comment parentComment = validateParentComment(request.getParentCommentId());
        UUID postId = parentComment.getPost().getId();
        Comment comment = buildComment(postId, currentUserId, request.getContent(), 1,
                parentComment.getId());

        commentRepository.saveAndFlush(comment);

        CompletableFuture.runAsync(() -> handleAsyncCommentCreation(postId, parentComment.getId()));

        return buildCommentResponse(comment, currentUserId);
    }

    private CommentDTO buildCommentResponse(Comment comment, UUID currentUser) {
        CommentDTO result = commentMapper.toDTO(comment);
        result.setCreator(userMapper.toCreatorDTO(userService.findById(currentUser)));
        return result;
    }

    private void handleAsyncCommentCreation(UUID postId, UUID parentCommentId) {
        // Run the following code asynchronously
        CompletableFuture.runAsync(() -> {
            // Create an example Comment with the provided postId and isDeleted set to false
            Example<Comment> example = Example.of(Comment.builder()
                    .post(Post.builder().id(postId).build())
                    .isDeleted(false)
                    .build());
            // Update the comment count of the post with the count of comments matching the example
            postService.updateCommentCount(postId, (int) commentRepository.count(example));

            // Create a Redis key for the post comments
            String redisKey = RedisKeyUtil.getPostCommentsKey(postId, 0, 0);
            // Delete the Redis cache for the post comments
            redisService.deleteByPrefix(redisKey.substring(0, redisKey.indexOf("page")));

            // If a parentCommentId is provided, update the reply count of the parent comment
            if (parentCommentId != null) {
                updateReplyCount(parentCommentId);
            }
        });
    }

    private Comment buildComment(UUID postId, UUID userId, String content, int level, UUID parentCommentId) {
        return Comment.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(userId).build())
                .content(content)
                .parentComment(parentCommentId != null ? Comment.builder().id(parentCommentId).build() : null)
                .likeCount(0)
                .replyCount(0)
                .level(level)
                .isDeleted(false)
                .build();
    }

    private Comment validateParentComment(UUID parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (parentComment.getLevel() == 1) {
            throw new IllegalArgumentException("Cannot reply to a reply comment");
        }

        return parentComment;
    }

    private void validatePostExists(UUID postId) {
        if (!postService.isExist(postId)) {
            throw new ResourceNotFoundException("Post not found");
        }
    }

    private void cacheComments(String redisKey, List<CommentDTO> comments) {
        redisService.saveList(redisKey, comments);
        if (comments.isEmpty()) {
            redisService.setTTL(redisKey, 5, TimeUnit.MINUTES);
        } else {
            redisService.setTTL(redisKey, 1, TimeUnit.DAYS);
        }
    }

    private List<CommentDTO> fetchCommentsFromDatabase(Example<Comment> example, Pageable pageable) {
        List<Comment> content = commentRepository.findAll(example, pageable).getContent();
        return commentMapper.toDTOs(content);
    }
}
