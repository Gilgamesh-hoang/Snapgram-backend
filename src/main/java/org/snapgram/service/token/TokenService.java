package org.snapgram.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.SystemConstant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService implements ITokenService {
    JwtHelper jwtHelper;
    IRedisService redisService;
    @Override
    public void deleteExpiredTokens() {
        List<Object> tokenExpired = new ArrayList<>();
        redisService.getMap(SystemConstant.BLACKLIST_TOKEN).forEach((key, value) -> {
            TokenDTO token = (TokenDTO) value;
            // check token expired
            if (token.getExpiredDate().before(new Date())) {
                tokenExpired.add(key);
            }
        });
        redisService.deleteElementsFromMap(SystemConstant.BLACKLIST_TOKEN, tokenExpired);
    }

    @Override
    public void saveAll(String accessToken, String refreshToken) {
        TokenDTO accessObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromToken(accessToken))
                .build();
        TokenDTO refreshObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromRefreshToken(refreshToken))
                .build();
        String accessId = jwtHelper.getJidFromToken(accessToken);
        String refreshId = jwtHelper.getJidFromRefreshToken(refreshToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(accessId, accessObj);
        map.put(refreshId, refreshObj);
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
    }

    @Override
    public TokenDTO findToken(String token, boolean isRefreshToken) {
        String jid;
        if (isRefreshToken) {
            jid = jwtHelper.getJidFromRefreshToken(token);
        } else {
            jid = jwtHelper.getJidFromToken(token);
        }
        return redisService.getElementFromMap(SystemConstant.BLACKLIST_TOKEN, jid, TokenDTO.class);
    }


}
