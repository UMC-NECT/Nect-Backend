package com.nect.api.scheduler.domain;

import com.nect.api.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingScheduler {

    private final MatchingService matchingService;

    @Scheduled(cron = "0 */1 * * * *")
    public void expireMatchings() {
        log.info("MatchingScheduler.expireMatchings() invoked");
        int expired = matchingService.expireDueMatchings();
        log.info("expired count: {}", expired);
    }
}
