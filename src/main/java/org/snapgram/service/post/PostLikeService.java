package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostLike;
import org.snapgram.entity.database.User;
import org.snapgram.repository.database.PostLikeRepository;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostLikeService implements IPostLikeService {
    PostLikeRepository postLikeRepository;

    @Override
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, user));
        return postLikeRepository.exists(example);
    }

    @Override
    @Transactional
    public void like(UUID postId) {
        Post post = Post.builder().id(postId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isPostLikedByUser(postId, user.getId())) {
            return;
        }
        PostLike postLike = new PostLike(post, user);
        postLikeRepository.save(postLike);
    }

    @Override
    @Transactional
    public void unlike(UUID postId) {
        Post post = Post.builder().id(postId).build();
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        postLikeRepository.deleteByPostAndUser(post, user);
    }

    @Override
    public int countByPost(UUID postId) {
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, null));
        return (int) postLikeRepository.count(example);
    }
}
