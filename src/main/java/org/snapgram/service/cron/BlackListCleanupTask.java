package org.snapgram.service.cron;

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

    @Scheduled(cron = "0 0 0 */2 * ?")
//    @Scheduled(fixedRate = 2000)
    private void deleteExpiredTokens() {
        log.info("Running token cleanup task...");
        tokenService.deleteExpiredTokensFromBlacklist();
    }
}
