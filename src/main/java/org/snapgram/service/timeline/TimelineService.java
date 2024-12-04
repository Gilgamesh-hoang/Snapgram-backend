package org.snapgram.service.timeline;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.timeline.Timeline;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.TimelineRepository;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.timeline.strategy.ActiveUserTimelineStrategy;
import org.snapgram.service.timeline.strategy.InactiveUserTimelineStrategy;
import org.snapgram.service.timeline.strategy.TimelineStrategy;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimelineService implements ITimelineService {
    IFollowService followService;
    TimelineRepository timelineRepository;
    IRedisService redisService;
    RedisProducer redisProducer;
    IPostService postService;

    @Override
    @Transactional
    public List<PostDTO> getTimelinesByUser(UUID userId, Pageable pageable) {
        boolean isInactive = redisService.containInSet(RedisKeyUtil.USERS_INACTIVE, userId);
        TimelineStrategy strategy = isInactive ?
                new InactiveUserTimelineStrategy(followService, timelineRepository, redisService, redisProducer, postService)
                : new ActiveUserTimelineStrategy(timelineRepository, redisService, redisProducer, postService);
        return strategy.getTimeline(userId, pageable);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> generateTimeline(UUID creatorId, UUID postId, Timestamp createdAt) {
        boolean hasMore = true;
        int page = 0;
        int pageSize = 100;
        int batchSize = 10;
        List<Timeline> batchTimelines = new ArrayList<>();

        while (hasMore) {
            List<UUID> followerIds = followService.getFollowersByUser(creatorId, Pageable.ofSize(pageSize).withPage(page))
                    .stream().map(UserDTO::getId).toList();

            Set<UUID> inactiveUsers = redisService.getSet(RedisKeyUtil.USERS_INACTIVE);

            followerIds = followerIds.stream().filter(id -> !inactiveUsers.contains(id)).toList();

            if (followerIds.isEmpty()) {
                hasMore = false;
            } else {
                List<Timeline> timelines = followerIds.stream().map(followerId ->
                        Timeline.builder().userId(followerId).postId(postId).postCreatedAt(createdAt).build()).toList();

                // Thêm vào batch
                batchTimelines.addAll(timelines);

                // Nếu batch đã đủ size, lưu vào database
                if (batchTimelines.size() >= batchSize * pageSize) {
                    timelineRepository.saveAll(batchTimelines);
                    batchTimelines.clear(); // Xóa batch sau khi lưu
                }

                page++;
            }
        }

        if (!batchTimelines.isEmpty()) {
            timelineRepository.saveAll(batchTimelines);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> addPostsToTimeline(UUID followerId, UUID followeeId) {
        final int PAGE_SIZE = 25;
        final int PAGE = 0;

        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(PAGE, PAGE_SIZE, sort);
        List<Timeline> timelines = postService.getPostsByUser(followeeId, pageable)
                .stream().map(post ->
                        Timeline.builder().userId(followerId).postId(post.getId()).postCreatedAt(post.getCreatedAt()).build()
                ).toList();

        timelineRepository.saveAll(timelines);
        return null;
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> removePostsFromTimeline(UUID followerId, UUID followeeId) {
        List<UUID> posts = postService.getPostsByUser(followeeId, Pageable.unpaged()).stream().map(PostDTO::getId).toList();
        if (!posts.isEmpty()) {
            timelineRepository.deleteAllByUserIdAndPostIds(followerId, posts);
        }
        return null;
    }
}
