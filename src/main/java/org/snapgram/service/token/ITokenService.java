package org.snapgram.service.token;

import org.snapgram.dto.response.TokenDTO;

import java.util.UUID;

public interface ITokenService {

    void deleteExpiredTokensFromBlacklist();


    void saveATInBlacklist(String accessToken);
    void saveAllInBlacklist(String accessToken, String refreshToken);

    TokenDTO findTokenInBlacklist(String token, boolean isRefreshToken);

    void saveRTInDB(String refreshToken, UUID userId);
    void saveRTInDB(String oldRT, String newRT);

    void deleteRefreshTokenInDB(String refreshToken);

    void saveRTInBlacklist(String refreshToken);

    void saveAllRTInBlacklist(UUID userId);
    void saveAllRTInBlacklist(UUID userId, String ...exceptToken);
}
