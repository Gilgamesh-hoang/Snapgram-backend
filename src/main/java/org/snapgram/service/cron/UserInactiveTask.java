package org.snapgram.service.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInactiveTask {
    private final IRedisService redisService;
    private static final long DAYS = 5L * 24L * 60L * 60L * 1000L;

//    @Scheduled(fixedRate = 10000)
    @Transactional
    public void handleUserInactive() {
        Set<UUID> userIds = new HashSet<>();
        redisService.getMap(RedisKeyUtil.LAST_REFRESH_TOKEN).forEach((key, value) -> {
            UUID userId = (UUID) key;
            Timestamp lastRefreshToken = (Timestamp) value;

            // Nếu thời gian refresh token cuối cùng cách thời điểm hiện tại quá 5 ngày
            if (lastRefreshToken.getTime() + DAYS < System.currentTimeMillis()) {
                userIds.add(userId);
            }
        });
        if (!userIds.isEmpty()) {
            redisService.saveSet(RedisKeyUtil.USERS_INACTIVE, userIds);
        }
    }
}
