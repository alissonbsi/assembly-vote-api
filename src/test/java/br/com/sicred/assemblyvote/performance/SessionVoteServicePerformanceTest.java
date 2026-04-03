package br.com.sicred.assemblyvote.performance;

import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.client.ValidateMemberClient;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VoteRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.mapper.VoteMapper;
import br.com.sicred.assemblyvote.mapper.VotingSessionMapper;
import br.com.sicred.assemblyvote.service.SessionVoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionVoteServicePerformanceTest {

    private SessionVoteService service;
    private VotingSessionRepository sessionRepository;
    private AgendaRepository agendaRepository;
    private SessionRedisRepository redisRepository;
    private VoteRepository voteRepository;
    private VoteMapper voteMapper;
    private VotingSessionMapper sessionMapper;
    private ValidateMemberClient client;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(VotingSessionRepository.class);
        agendaRepository = mock(AgendaRepository.class);
        redisRepository = mock(SessionRedisRepository.class);
        voteRepository = mock(VoteRepository.class);
        voteMapper = mock(VoteMapper.class);
        sessionMapper = mock(VotingSessionMapper.class);
        client = mock(ValidateMemberClient.class);

        service = new SessionVoteService(
                sessionRepository,
                agendaRepository,
                sessionMapper,
                redisRepository,
                voteRepository,
                voteMapper,
                client
        );
    }

    @Test
    void testOpenSessionPerformance() {
        UUID agendaId = UUID.randomUUID();
        var request = mock(br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest.class);

        var agenda = mock(br.com.sicred.assemblyvote.domain.model.AgendaEntity.class);
        when(agendaRepository.findById(agendaId)).thenReturn(Optional.of(agenda));
        when(sessionMapper.toEntity(any(), any(), any())).thenReturn(mock(br.com.sicred.assemblyvote.domain.model.VotingSessionEntity.class));
        when(sessionMapper.toRedis(any(), any())).thenReturn(mock(br.com.sicred.assemblyvote.cache.model.SessionRedis.class));
        when(sessionMapper.toResponse(any())).thenReturn(mock(br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse.class));

        long start = System.nanoTime();
        service.openSession(agendaId, request);
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000;
        System.out.println("openSession execution time: " + durationMs + " ms");
        assertTrue(durationMs < 500, "Execution should be under 500ms");
    }
}
