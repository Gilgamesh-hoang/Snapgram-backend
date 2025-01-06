package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.PostLikeDTO;
import org.snapgram.dto.kafka.PostLikeUpdateMessage;
import org.snapgram.dto.response.PostMetricDTO;
import org.snapgram.entity.database.post.Post;
import org.snapgram.entity.database.post.PostLike;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.NotificationType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.PostLikeProducer;
import org.snapgram.mapper.PostLikeMapper;
import org.snapgram.repository.database.PostLikeRepository;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.follow.IAffinityService;
import org.snapgram.service.notification.INotificationService;
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
    PostLikeMapper mapper;
    INotificationService notificationService;

    @Override
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, user));
        return postLikeRepository.exists(example);
    }

    @Override
    public PostLikeDTO getPostLike(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<PostLike> example = Example.of(new PostLike(post, user));
        PostLike postLike = postLikeRepository.findOne(example).orElseThrow(() -> new ResourceNotFoundException("Post like not found"));
        return mapper.toDTO(postLike);
    }

    @Override
    @Transactional
    public PostMetricDTO like(UUID postId) {
        Post post = getPostByIdOrThrow(postId);
        User user = User.builder().id(UserSecurityHelper.getCurrentUser().getId()).build();
        if (!isPostLikedByUser(postId, user.getId())) {

            PostLike postLike = new PostLike(post, user);
            postLikeRepository.save(postLike);

            likeProducer.sendUpdateLike(
                    PostLikeUpdateMessage.builder().postId(postId).action(PostLikeUpdateMessage.Action.INCREMENT).build()
            );
//            post.setLikeCount(postLikeService.countByPost(postId));
//            postRepository.save(post);

            notificationService.createNotification(CreateNotifyDTO.builder()
                    .type(NotificationType.LIKE_POST)
                    .entityId(postId)
                    .actorId(user.getId())
                    .build());

            affinityService.increaseAffinityByLike(user.getId(), postId);
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
