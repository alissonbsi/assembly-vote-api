package br.com.sicred.assemblyvote.scheduler;

import br.com.sicred.assemblyvote.service.SessionVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionScheduler {

    private final SessionVoteService sessionVoteService;

    @Scheduled(fixedDelayString = "${scheduler.voting.close-delay}")
    public void closeExpiredSessions() {
        final var qtdeUpdated = sessionVoteService.closeExpiredSessions();

        log.info("Closed {} sessions", qtdeUpdated);
    }
}
