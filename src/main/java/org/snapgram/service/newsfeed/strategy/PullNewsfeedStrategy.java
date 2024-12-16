package org.snapgram.service.newsfeed.strategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.timeline.Timeline;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.NewsfeedRepository;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PullNewsfeedStrategy implements NewsfeedStrategy {
    final IFollowService followService;
    final NewsfeedRepository newsfeedRepository;
    final IRedisService redisService;
    final RedisProducer redisProducer;
    final IPostService postService;
    int pageOfFollow = 0;
    static final int SIZE_OF_FOLLOW = 20;

    @Override
    public List<PostDTO> getNewsfeed(UUID userId, Pageable pageable) {
        // Remove user from inactive set
        redisProducer.sendDeleteItemsInSet(RedisKeyUtil.USERS_INACTIVE, List.of(userId));

        // Get last active timestamp
        Timestamp lastTime = redisService.getElementFromMap(RedisKeyUtil.GET_TIMELINE_LATEST, userId, Timestamp.class);

        // Fetch initial posts for first 2 pages
        List<PostDTO> results = fetchInitialPosts(userId, lastTime, pageable.getPageSize());

        // Save second page to database
        saveSecondPageToTimeline(userId, results.subList(pageable.getPageSize(), results.size()));

        // Process remaining posts in background
        processRemainingPostsInBackground(userId, lastTime);

        // Return the first page
        return results.subList(0, pageable.getPageSize());
    }


    private void processRemainingPostsInBackground(UUID userId, Timestamp lastTime) {
        CompletableFuture.runAsync(() -> {
            int batchSize = 100;
            boolean hasMore = true;
            List<Timeline> batchTimelines = new ArrayList<>();

            while (hasMore) {
                List<UUID> followeesIds = followService.getFolloweesByUser(userId, PageRequest.of(pageOfFollow, SIZE_OF_FOLLOW))
                        .stream().map(UserDTO::getId).toList();

                if (followeesIds.isEmpty()) {
                    hasMore = false;
                } else {
                    List<PostDTO> posts = postService.getPostsByUsersAndAfter(followeesIds, lastTime);
                    batchTimelines.addAll(posts.stream().map(post ->
                            Timeline.builder().userId(userId).postId(post.getId()).postCreatedAt(post.getCreatedAt()).build()
                    ).toList());

                    if (batchTimelines.size() >= batchSize) {
                        newsfeedRepository.saveAll(batchTimelines);
                        batchTimelines.clear();
                    }
                    pageOfFollow++;
                }
            }

            if (!batchTimelines.isEmpty()) {
                newsfeedRepository.saveAll(batchTimelines);
            }
        });
    }

    private List<PostDTO> fetchInitialPosts(UUID userId, Timestamp lastTime, int pageSize) {
        List<PostDTO> results = new ArrayList<>();
        boolean hasMore = true;

        while (hasMore) {
            List<UUID> followeesIds = followService.getFolloweesByUser(userId, PageRequest.of(pageOfFollow, SIZE_OF_FOLLOW))
                    .stream().map(UserDTO::getId).toList();

            if (followeesIds.isEmpty()) {
                hasMore = false;
            } else {
                results.addAll(postService.getPostsByUsersAndAfter(followeesIds, lastTime));
                if (results.size() >= 2 * pageSize) {
                    hasMore = false;
                }
                pageOfFollow++;
            }
        }
        return results;
    }

    private void saveSecondPageToTimeline(UUID userId, List<PostDTO> secondPage) {
        CompletableFuture.runAsync(() -> {
            List<Timeline> timelines = secondPage.stream().map(post ->
                    Timeline.builder().userId(userId).postId(post.getId()).postCreatedAt(post.getCreatedAt()).build()
            ).toList();
            newsfeedRepository.saveAll(timelines);
        });
    }
}
