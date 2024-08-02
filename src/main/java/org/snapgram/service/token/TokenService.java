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

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService implements ITokenService {
    JwtHelper jwtHelper;
    IRedisService redisService;

    @Override
    public void delete(String token) {
    }

    @Override
    public void saveAll(String accessToken,String refreshToken) {

        TokenDTO accessObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromToken(accessToken))
                .build();
        TokenDTO refreshObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromRefreshToken(refreshToken))
                .build();
        String accessId = jwtHelper.getJidFromToken(accessToken);
        String refreshId = jwtHelper.getJidFromRefreshToken(refreshToken);
        redisService.addElementToMap(SystemConstant.BLACKLIST_TOKEN, accessId, accessObj);
        redisService.addElementToMap(SystemConstant.BLACKLIST_TOKEN, refreshId, refreshObj);
    }

    @Override
    public TokenDTO findToken(String token) {
        String jid = jwtHelper.getJidFromRefreshToken(token);
        return redisService.getElementFromMap(SystemConstant.BLACKLIST_TOKEN, jid, TokenDTO.class);
    }


}
