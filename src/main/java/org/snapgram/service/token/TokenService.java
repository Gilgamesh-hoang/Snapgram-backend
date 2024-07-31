package org.snapgram.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.jwt.JwtHelper;
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
    public void save(String token) {
        TokenDTO tokenDTO = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpiration(token))
                .build();
        String jid = jwtHelper.getJidFromToken(token);
        redisService.addElementToMap(SystemConstant.BLACKLIST_TOKEN, jid, tokenDTO);
        redisService.getMap(SystemConstant.BLACKLIST_TOKEN).forEach((k, v) -> log.info("key: " + k + " value: " + v));
    }

    @Override
    public TokenDTO findToken(String token) {
        String jid = jwtHelper.getJidFromToken(token);
        return redisService.getElementFromMap(SystemConstant.BLACKLIST_TOKEN, jid, TokenDTO.class);
    }


}
