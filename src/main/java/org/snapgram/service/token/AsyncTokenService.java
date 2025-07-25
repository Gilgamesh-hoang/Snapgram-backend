package org.snapgram.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AsyncTokenService {
    @Value("${jwt.refresh_token.duration}")
    @NonFinal
    long REFRESH_TOKEN_LIFETIME;
    JwtHelper jwtHelper;
    RedisProducer redisProducer;

    @Async
    public CompletableFuture<Void> blacklistAccessToken(String accessToken) {
        TokenDTO accessObj = TokenDTO.builder()
                .expiredDate(jwtHelper.getExpiryFromAccessToken(accessToken))
                .build();
        String accessId = jwtHelper.getJidFromAccessToken(accessToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(accessId, accessObj);
        redisProducer.sendSaveMap(RedisKeyUtil.JWT_BLACKLIST, map);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> blacklistRefreshToken(String refreshToken) {
        TokenDTO refreshObj = TokenDTO.builder()
                .expiredDate(new Date(jwtHelper.getRefreshTokenExpiry(refreshToken).getTime() + REFRESH_TOKEN_LIFETIME))
                .build();
        String refreshId = jwtHelper.getJidFromRefreshToken(refreshToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(refreshId, refreshObj);
        redisProducer.sendSaveMap(RedisKeyUtil.JWT_BLACKLIST, map);
        return CompletableFuture.completedFuture(null);
    }
}
