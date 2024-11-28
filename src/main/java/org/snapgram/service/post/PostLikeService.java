package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.kafka.PostLikeUpdateMessage;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostLike;
import org.snapgram.entity.database.User;
import org.snapgram.kafka.producer.PostLikeProducer;
import org.snapgram.repository.database.PostLikeRepository;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostLikeService implements IPostLikeService {
    PostLikeRepository postLikeRepository;
    PostLikeProducer likeProducer;
    @Override
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, user));
        return postLikeRepository.exists(example);
    }

    @Override
    @Transactional
    public boolean like(UUID postId) {
        Post post = Post.builder().id(postId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isPostLikedByUser(postId, user.getId())) {
            return false;
        }
        likeProducer.sendUpdateLike(
                PostLikeUpdateMessage.builder().postId(postId).action(PostLikeUpdateMessage.Action.INCREMENT).build()
        );
        PostLike postLike = new PostLike(post, user);
        postLikeRepository.save(postLike);
        return true;
    }

    @Override
    @Transactional
    public boolean unlike(UUID postId) {
        Post post = Post.builder().id(postId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isPostLikedByUser(postId, user.getId())) {
            likeProducer.sendUpdateLike(
                    PostLikeUpdateMessage.builder().postId(postId).action(PostLikeUpdateMessage.Action.DECREMENT).build()
            );
            postLikeRepository.deleteByPostAndUser(post, user);
            return true;
        }
        return false;

    }

    @Override
    public int countByPost(UUID postId) {
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, null));
        return (int) postLikeRepository.count(example);
    }

    @Override
    public List<UUID> getLikedPosts(UUID currentUserId, List<UUID> postIds) {
        return postLikeRepository.findLikedPosts(currentUserId, postIds);
    }
}
