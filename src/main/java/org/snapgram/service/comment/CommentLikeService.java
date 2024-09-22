package org.snapgram.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.database.Comment;
import org.snapgram.entity.database.CommentLike;
import org.snapgram.entity.database.User;
import org.snapgram.repository.database.CommentLikeRepository;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentLikeService implements ICommentLikeService {
    CommentLikeRepository likeRepository;

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

    @Override
    public void like(UUID commentId) {
        Comment comment = Comment.builder().id(commentId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isCommentLikedByUser(commentId, user.getId())) {
            return;
        }
        CommentLike like = CommentLike.builder()
                .comment(comment)
                .user(user)
                .build();
        likeRepository.save(like);
    }

    @Override
    @Transactional
    public void unlike(UUID commentId) {
        Comment comment = Comment.builder().id(commentId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        Example<CommentLike> example = Example.of(
                CommentLike.builder()
                        .comment(comment)
                        .user(user)
                        .build());
        likeRepository.findOne(example).ifPresent(likeRepository::delete);
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
