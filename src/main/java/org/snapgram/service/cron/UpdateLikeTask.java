package org.snapgram.service.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateLikeTask {
    private final IRedisService redisService;
    private final IPostService postService;

    @Scheduled(fixedRate = 10000)  // Chạy mỗi 10 giây (fixed rate)
    @Transactional
    public void fetchAndDeleteHash() {
        Map<Object, Object> likes = redisService.popAllElementsFromMapWithLock(RedisKeyUtil.POST_LIKE_COUNT);
        log.info("Processing {} likes", likes.size());
        for (Map.Entry<Object, Object> entry : likes.entrySet()) {
            UUID postId = UUID.fromString((String) entry.getKey());
            Integer likeCount = (Integer) entry.getValue();
            postService.updateLikeCount(postId, likeCount);
        }
    }
}
