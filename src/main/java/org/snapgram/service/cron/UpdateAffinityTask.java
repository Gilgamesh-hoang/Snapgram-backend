package org.snapgram.service.cron;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.service.follow.IAffinityService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.AppConstant;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateAffinityTask {
    IRedisService redisService;
    IAffinityService affinityService;

//    @Scheduled(fixedRate = 10000)  // Chạy mỗi 10 giây (fixed rate)
    @Transactional
    public void fetchAndDeleteHash() {
        Map<Object, Object> affinities = redisService.popAllEntriesFromMapWithLock(RedisKeyUtil.AFFINITY);
        for (Map.Entry<Object, Object> entry : affinities.entrySet()) {
            String[] ids = entry.getKey().toString().split(AppConstant.AFFINITY_SEPARATOR);
            UUID followerId = UUID.fromString(ids[0]);
            UUID followeeId = UUID.fromString(ids[1]);
            affinityService.updateAffinity(
                    AffinityDTO.builder()
                            .followerId(followerId)
                            .followeeId(followeeId)
                            .closeness(((Integer) entry.getValue()).floatValue())
                            .build()
            );
        }
    }
}
