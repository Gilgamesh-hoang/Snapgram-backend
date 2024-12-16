package org.snapgram.service.newsfeed;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.timeline.Timeline;
import org.snapgram.repository.database.NewsfeedRepository;
import org.snapgram.service.newsfeed.strategy.NewsfeedStrategy;
import org.snapgram.service.newsfeed.strategy.PullNewsfeedStrategy;
import org.snapgram.service.newsfeed.strategy.PushNewsfeedStrategy;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewsfeedService implements INewsfeedService {
    NewsfeedRepository newsfeedRepository;
    IRedisService redisService;
    IPostService postService;
    PullNewsfeedStrategy pullStrategy;
    PushNewsfeedStrategy pushStrategy;

    @Override
    public List<PostDTO> getNewsfeedByUser(UUID userId, Pageable pageable) {
        boolean isInactive = redisService.containInSet(RedisKeyUtil.USERS_INACTIVE, userId);
        NewsfeedStrategy strategy = isInactive ? pullStrategy : pushStrategy;
        return strategy.getNewsfeed(userId, pageable);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> generateNewsfeed(UUID creatorId, UUID postId, Timestamp createdAt) {
        pushStrategy.generateNewsfeed(creatorId, postId, createdAt);
        return null;
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> addPostsToNewsfeed(UUID followerId, UUID followeeId) {
        // 7 days before
        final int DAYS = 7 * 24 * 60 * 60 * 1000;
        Timestamp time = new Timestamp(System.currentTimeMillis() - DAYS);
        List<Timeline> timelines = postService.getPostsByUsersAndAfter(List.of(followeeId), time)
                .stream().map(post ->
                        Timeline.builder().userId(followerId).postId(post.getId()).postCreatedAt(post.getCreatedAt()).build()
                ).toList();

        if (!timelines.isEmpty()) {
            newsfeedRepository.saveAll(timelines);
        }
        return null;
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> removePostsInNewsfeed(UUID followerId, UUID followeeId) {
        List<UUID> posts = postService.getPostsByUser(followeeId, Pageable.unpaged()).stream().map(PostDTO::getId).toList();
        if (!posts.isEmpty()) {
            newsfeedRepository.deleteAllByUserIdAndPostIds(followerId, posts);
        }
        return null;
    }
}
