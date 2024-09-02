package org.snapgram.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.SystemConstant;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AsyncTokenService {
    JwtHelper jwtHelper;
    IRedisService redisService;

    @Async
    public CompletableFuture<Void> blacklistAccessToken(String accessToken) {
        TokenDTO accessObj = TokenDTO.builder()
                .expiredDate(jwtHelper.getExpiryFromAccessToken(accessToken))
                .build();
        String accessId = jwtHelper.getJidFromAccessToken(accessToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(accessId, accessObj);
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
        return CompletableFuture.completedFuture(null);
    }
    @Async
    public CompletableFuture<Void> blacklistRefreshToken(String refreshToken) {
        TokenDTO refreshObj = TokenDTO.builder()
                .expiredDate(jwtHelper.getRefreshTokenExpiry(refreshToken))
                .build();
        String refreshId = jwtHelper.getJidFromRefreshToken(refreshToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(refreshId, refreshObj);
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
        return CompletableFuture.completedFuture(null);
    }
}
