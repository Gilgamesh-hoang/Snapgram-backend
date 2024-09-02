package org.snapgram.service.token;

import org.snapgram.dto.response.TokenDTO;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ITokenService {

    void removeExpiredTokens();


    CompletableFuture<Void> blacklistAccessToken(String accessToken);
    void blacklistTokens(String accessToken, String refreshToken);

    TokenDTO getTokenFromBlacklist(String token, boolean isRefreshToken);
    TokenDTO getTokenFromBlacklist(String jid);

    CompletableFuture<Void> storeRefreshToken(String refreshToken, UUID userId);
    CompletableFuture<Void> storeRefreshToken(String oldRT, String newRT);

    void removeRefreshToken(String refreshToken);

    CompletableFuture<Void> blacklistRefreshToken(String refreshToken);

    CompletableFuture<Void> blacklistAllUserTokens(UUID userId);
    void blacklistAllUserTokens(UUID userId, String ...exceptToken);

}
