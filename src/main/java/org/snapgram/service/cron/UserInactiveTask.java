package org.snapgram.service.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.scheduling.annotation.Scheduled;
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

    // Chạy mỗi 5 ngày
    @Scheduled(fixedRate = DAYS)
    private void handleUserInactive() {
        Set<UUID> userIds = new HashSet<>();
        redisService.getMap(RedisKeyUtil.LAST_REFRESH_TOKEN).forEach((key, value) -> {
            UUID userId = UUID.fromString((String) key);
            Timestamp lastRefreshToken;

            if (value instanceof Long valueLong) {
                lastRefreshToken = new Timestamp(valueLong);
            } else if (value instanceof Timestamp valueTimestamp) {
                lastRefreshToken = valueTimestamp;
            } else {
                log.error("Invalid type for last refresh token: {}", value.getClass());
                return; // Skip this entry if the type is invalid
            }


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
