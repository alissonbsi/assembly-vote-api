package br.com.sicred.assemblyvote.scheduler;

import br.com.sicred.assemblyvote.service.SessionVoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.qos.logback.core.testUtil.RandomUtil.getPositiveInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionSchedulerTest {

    @InjectMocks
    private SessionScheduler sessionScheduler;

    @Mock
    private SessionVoteService sessionVoteService;

    @Test
    void shouldCloseExpiredSessions() {
        final var mockedClosedSessions = getPositiveInt();
        when(sessionVoteService.closeExpiredSessions()).thenReturn(mockedClosedSessions);

        sessionScheduler.closeExpiredSessions();

        verify(sessionVoteService, times(1)).closeExpiredSessions();
    }
}
