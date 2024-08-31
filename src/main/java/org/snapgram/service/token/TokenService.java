package org.snapgram.service.token;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.entity.database.Token;
import org.snapgram.entity.database.User;
import org.snapgram.repository.database.TokenRepository;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.SystemConstant;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService implements ITokenService {
    TokenRepository tokenRepository;
    JwtHelper jwtHelper;
    IRedisService redisService;

    @Override
    public void deleteExpiredTokensFromBlacklist() {
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
    public void saveATInBlacklist(String accessToken) {
        TokenDTO accessObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromToken(accessToken))
                .build();
        String accessId = jwtHelper.getJidFromToken(accessToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(accessId, accessObj);
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
    }

    @Override
    public void saveRTInBlacklist(String refreshToken) {
        TokenDTO refreshObj = TokenDTO.builder()
                .expiredDate(jwtHelper.extractExpirationFromRefreshToken(refreshToken))
                .build();
        String refreshId = jwtHelper.getJidFromRefreshToken(refreshToken);
        HashMap<String, Object> map = new HashMap<>();
        map.put(refreshId, refreshObj);
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
    }

    @Override
    public void saveAllRTInBlacklist(UUID userId) {
        List<Token> tokens = tokenRepository.findByUser(User.builder().id(userId).build());
        HashMap<String, Object> tokenMap = new HashMap<>();
        tokens.forEach(token -> {
            // check if token is expired
            if (!token.getExpireAt().before(new Date())) {
                TokenDTO refreshObj = TokenDTO.builder()
                        .expiredDate(token.getExpireAt())
                        .build();
                tokenMap.put(token.getRefreshTokenId().toString(), refreshObj);
            }
        });
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, tokenMap);
        tokenRepository.deleteAllById(tokens.stream().map(Token::getRefreshTokenId).toList());
    }

    @Override
    public void saveAllRTInBlacklist(UUID userId, String... exceptToken) {
        List<Token> tokens = tokenRepository.findByUser(User.builder().id(userId).build());
        HashMap<String, Object> tokenMap = new HashMap<>();
        List<UUID> exceptArr = Arrays.stream(exceptToken).map(token ->
                UUID.fromString(jwtHelper.getJidFromRefreshToken(token))
        ).toList();
        List<Token> temp = new ArrayList<>();
        tokens.forEach(token -> {
            // check if token is expired
            if (!token.getExpireAt().before(new Date()) && !exceptArr.contains(token.getRefreshTokenId())) {
                TokenDTO refreshObj = TokenDTO.builder()
                        .expiredDate(token.getExpireAt())
                        .build();
                tokenMap.put(token.getRefreshTokenId().toString(), refreshObj);
                temp.add(token);
            }
        });
        redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, tokenMap);
        tokenRepository.deleteAllById(temp.stream().map(Token::getRefreshTokenId).toList());
    }

    @Override
    public void saveAllInBlacklist(String accessToken, String refreshToken) {
        saveATInBlacklist(accessToken);
        saveRTInBlacklist(refreshToken);
    }

    @Override
    public TokenDTO findTokenInBlacklist(String token, boolean isRefreshToken) {
        String jid;
        if (isRefreshToken) {
            jid = jwtHelper.getJidFromRefreshToken(token);
        } else {
            jid = jwtHelper.getJidFromToken(token);
        }
        return redisService.getElementFromMap(SystemConstant.BLACKLIST_TOKEN, jid, TokenDTO.class);
    }

    @Override
    public void saveRTInDB(String refreshToken, UUID userId) {
        // check if user has 3 refresh token
        User user = User.builder().id(userId).build();
        List<Token> tokens = tokenRepository.findByUser(user);
        if (tokens.size() == 3) {
            tokens.sort(Comparator.comparing(Token::getExpireAt));
            Token token = tokens.get(0);
            // check if token is expired
            if (!token.getExpireAt().before(new Date())) {
                TokenDTO refreshObj = TokenDTO.builder()
                        .expiredDate(token.getExpireAt())
                        .build();
                HashMap<String, Object> map = new HashMap<>();
                map.put(token.getRefreshTokenId().toString(), refreshObj);
                redisService.addElementsToMap(SystemConstant.BLACKLIST_TOKEN, map);
            }
            tokenRepository.delete(token);
        }
        Token token = Token.builder()
                .refreshTokenId(UUID.fromString(jwtHelper.getJidFromRefreshToken(refreshToken)))
                .user(user)
                .expireAt(jwtHelper.extractExpirationFromRefreshToken(refreshToken))
                .build();
        tokenRepository.save(token);
    }

    @Override
    public void saveRTInDB(String oldRT, String newRT) {
        Token token = tokenRepository.findById(UUID.fromString(jwtHelper.getJidFromRefreshToken(oldRT)))
                .orElseThrow(() -> new NoSuchElementException("Token not found"));

        Token newToken = Token.builder()
                .refreshTokenId(UUID.fromString(jwtHelper.getJidFromRefreshToken(newRT)))
                .user(token.getUser())
                .expireAt(jwtHelper.extractExpirationFromRefreshToken(newRT))
                .build();
        tokenRepository.save(newToken);
        tokenRepository.delete(token);
    }

    @Override
    public void deleteRefreshTokenInDB(String refreshToken) {
        UUID refreshTokenId = UUID.fromString(jwtHelper.getJidFromRefreshToken(refreshToken));
        tokenRepository.deleteById(refreshTokenId);
    }


}
