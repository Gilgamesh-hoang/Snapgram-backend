package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.request.CommentRequest;
import org.snapgram.dto.request.ReplyCommentRequest;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.comment.Comment;
import org.snapgram.entity.database.post.Post;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.NotificationType;
import org.snapgram.enums.SentimentType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.PostProducer;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.mapper.CommentMapper;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.CommentRepository;
import org.snapgram.service.banned.BannedWordsService;
import org.snapgram.service.follow.IAffinityService;
import org.snapgram.service.notification.INotificationService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.sentiment.ISentimentService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    RedisProducer redisProducer;
    PostProducer postProducer;
    IAffinityService affinityService;
    INotificationService notificationService;
    BannedWordsService bannedService;
    ISentimentService sentimentService;

    @Override
    public List<CommentDTO> getCommentsByPost(UUID postId, Pageable pageable) {
        String redisKey = RedisKeyUtil.getPostCommentsKey(postId, pageable.getPageNumber(), pageable.getPageSize());
        List<CommentDTO> comments = redisService.getList(redisKey, CommentDTO.class);
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
        redisProducer.sendSaveList(redisKey, comments, 1L, TimeUnit.DAYS);
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
    @Transactional
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

        // Analyze the sentiment of the new content
        SentimentType type = sentimentService.analyzeSentiment(content);
        comment.setSentiment(type);

        commentRepository.save(comment);

        // Create a Redis key for the post comments
        String redisKey = RedisKeyUtil.getPostCommentsKey(comment.getPost().getId(), 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));

        return commentMapper.toDTO(comment);
    }

    @Override
    public int deleteComment(UUID currentUserId, UUID commentId) {
        // Find the comment in the repository, throw an exception if not found
        Comment comment = validateComment(commentId);

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
    public CommentDTO getCommentById(UUID commentId) {
        return commentMapper.toDTO(validateComment(commentId));
    }

    @Override
    public UUID getPostIdByComment(UUID commentId) {
        Comment comment = validateComment(commentId);
        return comment.getPost().getId();
    }

    @Override
    public PostDTO getPostByComment(UUID commentId) {
        Comment comment = validateComment(commentId);
        return postService.getPostById(comment.getPost().getId());
    }

    private Comment validateComment(UUID commentId) {
        Example<Comment> example = Example.of(Comment.builder()
                .id(commentId)
                .isDeleted(false)
                .build());
        return commentRepository.findOne(example).orElseThrow(
                () -> new ResourceNotFoundException("Comment not found"));
    }

    @Override
    public void updateReplyCount(UUID commentId) {
        Comment comment = validateComment(commentId);
        Example<Comment> example = Example.of(Comment.builder()
                .parentComment(Comment.builder().id(commentId).build())
                .level(1)
                .isDeleted(false)
                .build());
        comment.setReplyCount((int) commentRepository.count(example));
        commentRepository.saveAndFlush(comment);
    }

    @Override
    @Transactional
    public CommentDTO createComment(UUID currentUser, CommentRequest request) {
        UUID postId = request.getPostId();

        validatePostExists(postId);

        Comment comment = buildComment(postId, currentUser, bannedService.removeBannedWords(request.getContent()), 0, null);
        commentRepository.saveAndFlush(comment);

        CompletableFuture.runAsync(() -> handleAsyncCommentCreation(postId, null));

        affinityService.increaseAffinityByComment(currentUser, postId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.createNotification(CreateNotifyDTO.builder()
                        .type(NotificationType.COMMENT_POST)
                        .entityId(comment.getId())
                        .actorId(comment.getUser().getId())
                        .build());
            }
        });
        return buildCommentResponse(comment, currentUser);
    }

    @Override
    public CommentDTO createComment(UUID currentUserId, ReplyCommentRequest request) {
        Comment parentComment = validateParentComment(request.getParentCommentId());
        UUID postId = parentComment.getPost().getId();
        Comment comment = buildComment(postId, currentUserId, bannedService.removeBannedWords(request.getContent()), 1,
                parentComment.getId());
        commentRepository.saveAndFlush(comment);

        CompletableFuture.runAsync(() -> handleAsyncCommentCreation(postId, parentComment.getId()));
        affinityService.increaseAffinity(UserSecurityHelper.getCurrentUser().getId(), parentComment.getUser().getId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.createNotification(CreateNotifyDTO.builder()
                        .type(NotificationType.REPLY_COMMENT)
                        .entityId(comment.getId())
                        .actorId(comment.getUser().getId())
                        .build());
            }
        });

        return buildCommentResponse(comment, currentUserId);
    }

    private CommentDTO buildCommentResponse(Comment comment, UUID currentUser) {
        CommentDTO result = commentMapper.toDTO(comment);
        result.setCreator(userMapper.toCreatorDTO(userService.getById(currentUser)));
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
            postProducer.sendUpdateCommentCount(postId, (int) commentRepository.count(example));

            // Create a Redis key for the post comments
            String redisKey = RedisKeyUtil.getPostCommentsKey(postId, 0, 0);
            // Delete the Redis cache for the post comments
            redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));

            // If a parentCommentId is provided, update the reply count of the parent comment
            if (parentCommentId != null) {
                updateReplyCount(parentCommentId);
            }
        });
    }

    private Comment buildComment(UUID postId, UUID userId, String content, int level, UUID parentCommentId) {
        // Analyze the sentiment of the content
        SentimentType type = sentimentService.analyzeSentiment(content);

        return Comment.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(userId).build())
                .content(content)
                .sentiment(type)
                .parentComment(parentCommentId != null ? Comment.builder().id(parentCommentId).build() : null)
                .likeCount(0)
                .replyCount(0)
                .level(level)
                .isDeleted(false)
                .build();
    }

    private Comment validateParentComment(UUID parentCommentId) {
        Comment parentComment = validateComment(parentCommentId);

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


    private List<CommentDTO> fetchCommentsFromDatabase(Example<Comment> example, Pageable pageable) {
        List<Comment> content = commentRepository.findAll(example, pageable).getContent();
        return commentMapper.toDTOs(content);
    }
}
