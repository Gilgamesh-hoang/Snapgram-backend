package org.snapgram.service.cron;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.service.user.IUserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCleanupTask {
    private final IUserService userService;

    @PostConstruct
    public void init() {
        log.info("Running initial user cleanup on server startup...");
        deleteInactiveUsers();
    }

    // Set the schedule to run at 23:00 with a 3-day gap
    @Scheduled(cron = "0 0 23 */3 * ?")
    private void deleteInactiveUsers() {
        log.info("Running scheduled task to delete unverified users at {}", LocalDateTime.now());
        userService.deleteInactiveUsers(3);
    }
}
