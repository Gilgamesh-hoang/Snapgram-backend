package org.snapgram.service.cron;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.token.ITokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlackListCleanupTask {
    private final ITokenService tokenService;

    @PostConstruct
    public void init() {
        log.info("Running initial token cleanup on server startup...");
        deleteExpiredTokens();
    }

    @Scheduled(cron = "0 0 0 */2 * ?") // Runs every 2 days at midnight
    private void deleteExpiredTokens() {
        log.info("Running token cleanup task...");
        tokenService.removeExpiredTokens();
    }
}
