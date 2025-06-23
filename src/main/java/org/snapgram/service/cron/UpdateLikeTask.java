package org.snapgram.service.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateLikeTask {
    private final IRedisService redisService;
    private final IPostService postService;

    @Scheduled(fixedRate = 2000)  // Chạy mỗi 2 giây (fixed rate)
    private void fetchAndDeleteHash() {
        Map<Object, Object> likes = redisService.popAllEntriesFromMapWithLock(RedisKeyUtil.POST_LIKE_COUNT);
        if (!likes.isEmpty()) {
            log.info("Fetched {} likes from Redis", likes.size());
        }

        for (Map.Entry<Object, Object> entry : likes.entrySet()) {
            UUID postId = UUID.fromString((String) entry.getKey());
            Integer likeCount = (Integer) entry.getValue();
            postService.updateLikeCount(postId, likeCount);
        }
    }
}
