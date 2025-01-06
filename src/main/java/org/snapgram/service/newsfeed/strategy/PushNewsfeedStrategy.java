package org.snapgram.service.newsfeed.strategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.timeline.Timeline;
import org.snapgram.exception.MultiThreadException;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.NewsfeedRepository;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PushNewsfeedStrategy implements NewsfeedStrategy {
    NewsfeedRepository newsfeedRepository;
    IRedisService redisService;
    RedisProducer redisProducer;
    IPostService postService;
    IFollowService followService;

    @Override
    public List<PostDTO> getNewsfeed(UUID userId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        String redisKey = RedisKeyUtil.getTimelineKey(userId, page, size);
        List<PostDTO> cachedResults = redisService.getList(redisKey, PostDTO.class);
        if (cachedResults != null && !cachedResults.isEmpty()) {
            return cachedResults;
        }

        Example<Timeline> example = Example.of(Timeline.builder().userId(userId).build());
        List<UUID> postIds = newsfeedRepository.findAll(example).stream().map(Timeline::getPostId).toList();

        if (postIds.isEmpty()) {
            redisProducer.sendSaveList(redisKey, List.of(), 30L, TimeUnit.SECONDS);
            return new ArrayList<>();
        }

        // Fetch posts and rank them
        List<PostDTO> posts = postService.getPostsByIds(postIds);
        List<AffinityDTO> affinities = getAffinitiesForPosts(userId, posts);
        List<PostDTO> rankedPosts = rankPosts(posts, affinities);

        // Paginate results
        List<PostDTO> paginatedResults = paginateResults(rankedPosts, page, size);
        redisProducer.sendSaveList(redisKey, paginatedResults, 1L, TimeUnit.MINUTES);
//            timelineRepository.deleteAllByUserIdAndPostIds(userId, paginatedResults.stream().map(PostDTO::getId).toList();

        return paginatedResults;
    }

    private List<PostDTO> rankPosts(List<PostDTO> posts, List<AffinityDTO> affinities) {
        // Group posts by creator ID to reduce repetition
        Map<UUID, List<PostDTO>> postsByCreator = posts.stream()
                .collect(Collectors.groupingBy(post -> post.getCreator().getId()));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        // Create a list to hold Future objects.
        List<Future<List<Map.Entry<Float, PostDTO>>>> futures = new ArrayList<>();

        // loop through each Affinity and calculate the rank
        for (AffinityDTO affinity : affinities) {
            futures.add(executor.submit(() -> {
                List<PostDTO> userPosts = postsByCreator.getOrDefault(affinity.getFolloweeId(), List.of());
                float closeness = affinity.getCloseness();

                List<Map.Entry<Float, PostDTO>> rankedListForUser = new ArrayList<>();
                for (PostDTO post : userPosts) {
                    float weight = post.getCommentCount() * 0.7F + post.getLikeCount() * 0.3F;
                    long decayInMinutes = (System.currentTimeMillis() - post.getCreatedAt().getTime()) / 60000;
                    float rank = closeness * weight / (decayInMinutes + 0.1F);

                    rankedListForUser.add(Map.entry(rank * 10000, post));
                }

                return rankedListForUser;
            }));
        }

        // List to store rank and corresponding posts
        List<Map.Entry<Float, PostDTO>> rankedList = new ArrayList<>();
        try {
            for (Future<List<Map.Entry<Float, PostDTO>>> future : futures) {
                rankedList.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new MultiThreadException("Failed to rank posts", e);
        } finally {
            executor.shutdown();
        }

        // Sort the list in descending order by rank
        rankedList.sort(Map.Entry.<Float, PostDTO>comparingByKey().reversed());

        // Return the sorted list of posts
        return rankedList.stream().map(Map.Entry::getValue).toList();
    }


    private List<PostDTO> paginateResults(List<PostDTO> posts, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min((page + 1) * size, posts.size());
        if (fromIndex >= posts.size()) {
            return new ArrayList<>();
        }
        return posts.subList(fromIndex, toIndex);
    }

    private List<AffinityDTO> getAffinitiesForPosts(UUID userId, List<PostDTO> posts) {
        Set<UUID> creators = posts.stream()
                .map(post -> post.getCreator().getId())
                .collect(Collectors.toSet());
        return followService.getAffinities(userId, creators);
    }


    public void generateNewsfeed(UUID creatorId, UUID postId, Timestamp createdAt) {
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
                    newsfeedRepository.saveAll(batchTimelines);
                    batchTimelines.clear(); // Xóa batch sau khi lưu
                }

                page++;
            }
        }

        if (!batchTimelines.isEmpty()) {
            newsfeedRepository.saveAll(batchTimelines);
        }
    }

}
