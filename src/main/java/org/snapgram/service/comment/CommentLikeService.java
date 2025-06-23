package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.entity.database.comment.Comment;
import org.snapgram.entity.database.comment.CommentLike;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.NotificationType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.CommentLikeRepository;
import org.snapgram.repository.database.CommentRepository;
import org.snapgram.service.notification.INotificationService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentLikeService implements ICommentLikeService {
    CommentLikeRepository likeRepository;
    INotificationService notificationService;
    CommentRepository commentRepository;
    RedisProducer redisProducer;

    @Override
    public boolean isCommentLikedByUser(UUID commentId, UUID userId) {
        User user = User.builder().id(userId).build();
        Comment comment = Comment.builder().id(commentId).build();
        Example<CommentLike> example = Example.of(
                CommentLike.builder()
                        .comment(comment)
                        .user(user)
                        .build());
        return likeRepository.exists(example);
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
    @Transactional
    public int like(UUID commentId) {
        Comment comment = validateComment(commentId);
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isCommentLikedByUser(commentId, user.getId())) {
            return comment.getLikeCount();
        }
        CommentLike like = CommentLike.builder()
                .comment(comment)
                .user(user)
                .build();
        likeRepository.save(like);

        comment.setLikeCount(countByComment(commentId));
        commentRepository.save(comment);

        // Create a Redis key for the post comments
        String redisKey = RedisKeyUtil.getPostCommentsKey(comment.getPost().getId(), 0, 0);
        // Delete the Redis cache for the post comments
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.createNotification(CreateNotifyDTO.builder()
                        .type(NotificationType.LIKE_COMMENT)
                        .entityId(commentId)
                        .actorId(user.getId())
                        .build());
            }
        });

        return comment.getLikeCount();
    }

    @Override
    @Transactional
    public int unlike(UUID commentId) {
        Comment comment = validateComment(commentId);
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        Example<CommentLike> example = Example.of(
                CommentLike.builder()
                        .comment(comment)
                        .user(user)
                        .build());
        likeRepository.findOne(example).ifPresent(likeRepository::delete);

        comment.setLikeCount(countByComment(commentId));
        commentRepository.save(comment);

        // Create a Redis key for the post comments
        String redisKey = RedisKeyUtil.getPostCommentsKey(comment.getPost().getId(), 0, 0);
        // Delete the Redis cache for the post comments
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));

        return comment.getLikeCount();
    }

    @Override
    public int countByComment(UUID commentId) {
        Example<CommentLike> example = Example.of(
                CommentLike.builder()
                        .comment(Comment.builder().id(commentId).build())
                        .build()
        );
        return (int) likeRepository.count(example);
    }

    @Override
    public List<UUID> filterLiked(UUID currentUserId, List<UUID> commentIds) {
        return likeRepository.findByCommentIdIn(currentUserId, commentIds);
    }
}

