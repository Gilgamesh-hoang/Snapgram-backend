package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.post.Post;
import org.snapgram.entity.database.post.Saved;
import org.snapgram.entity.database.user.User;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostSaveRepository;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostSaveService implements IPostSaveService {
    PostSaveRepository postSaveRepository;
    PostMapper postMapper;
    RedisProducer redisProducer;
    IRedisService redisService;

    @Override
    public boolean isPostSaveByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<Saved> example = Example.of(Saved.builder().post(post).user(user).build());
        return postSaveRepository.exists(example);
    }

    @Override
    public List<PostDTO> getSavedPostsByUser(UUID userId, Pageable pageable) {
        String redisKey = RedisKeyUtil.getSavedPostsKey(userId, pageable.getPageNumber(), pageable.getPageSize());
        List<PostDTO> results = redisService.getList(redisKey, PostDTO.class);
        if (results != null && !results.isEmpty()) {
            return results;
        }

        Example<Saved> example = Example.of(Saved.builder().user(User.builder().id(userId).build()).build());
        List<Saved> savedPosts = postSaveRepository.findAll(example, pageable).getContent();
        results = postMapper.toDTOs(savedPosts.stream().map(Saved::getPost).toList());
        redisProducer.sendSaveList(redisKey, results, 5L,  TimeUnit.MINUTES);
        return results;
    }

    @Override
    public void savePost(UUID postId) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        if (isPostSavedByUser(postId, currentUser.getId())) {
            return;
        }
        Saved saved = Saved.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(currentUser.getId()).build())
                .build();
        postSaveRepository.save(saved);

        String redisKey = RedisKeyUtil.getSavedPostsKey(currentUser.getId(), 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));
    }

    @Override
    public boolean isPostSavedByUser(UUID postId, UUID id) {
        User user = User.builder().id(id).build();
        Post post = Post.builder().id(postId).build();
        Example<Saved> example = Example.of(Saved.builder().post(post).user(user).build());
        return postSaveRepository.exists(example);
    }

    @Override
    @Transactional
    public void unsavedPost(UUID postId) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        postSaveRepository.deleteByPostIdAndUserId(postId, currentUser.getId());

        String redisKey = RedisKeyUtil.getSavedPostsKey(currentUser.getId(), 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));
    }

    @Override
    public List<UUID> getSavedPosts(UUID currentUserId, List<UUID> postIds) {
        return postSaveRepository.findSavedPosts(currentUserId, postIds);
    }
}
