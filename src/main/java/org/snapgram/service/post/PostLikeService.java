package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.kafka.PostLikeUpdateMessage;
import org.snapgram.dto.response.PostMetricDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostLike;
import org.snapgram.entity.database.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.PostLikeProducer;
import org.snapgram.repository.database.PostLikeRepository;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.follow.IAffinityService;
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
    PostRepository postRepository;
    IAffinityService affinityService;

    @Override
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, user));
        return postLikeRepository.exists(example);
    }

    @Override
    @Transactional
    public PostMetricDTO like(UUID postId) {
        Post post = getPostByIdOrThrow(postId);
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (!isPostLikedByUser(postId, user.getId())) {
            likeProducer.sendUpdateLike(
                    PostLikeUpdateMessage.builder().postId(postId).action(PostLikeUpdateMessage.Action.INCREMENT).build()
            );
            PostLike postLike = new PostLike(post, user);
            postLikeRepository.save(postLike);

//        post.setLikeCount(postLikeService.countByPost(postId));
//        postRepository.save(post);
            affinityService.increaseAffinityByLike(postId);
        }

        return buildPostMetricDTO(post);

    }

    @Override
    @Transactional
    public PostMetricDTO unlike(UUID postId) {
        Post post = getPostByIdOrThrow(postId);
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (isPostLikedByUser(postId, user.getId())) {
            likeProducer.sendUpdateLike(
                    PostLikeUpdateMessage.builder().postId(postId).action(PostLikeUpdateMessage.Action.DECREMENT).build()
            );
            postLikeRepository.deleteByPostAndUser(post, user);
        }
//        post.setLikeCount(postLikeService.countByPost(postId));
//        postRepository.save(post);
        return buildPostMetricDTO(post);

    }

    private Post getPostByIdOrThrow(UUID postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    private PostMetricDTO buildPostMetricDTO(Post post) {
        return PostMetricDTO.builder()
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
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
