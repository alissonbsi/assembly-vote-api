package br.com.sicred.assemblyvote.performance;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.client.ValidateMemberClient;
import br.com.sicred.assemblyvote.client.response.DocumentStatusResponse;
import br.com.sicred.assemblyvote.client.response.StatusMember;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VoteEntity;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static br.com.sicred.assemblyvote.fixture.Fixture.make;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionVoteServiceLoadTest {

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
    void testVoteSessionConcurrency() throws InterruptedException {
        UUID agendaId = UUID.randomUUID();
        var request = make(VoteRequest.class);
        var agenda = make(AgendaEntity.class);

        when(agendaRepository.findById(any())).thenReturn(Optional.of(agenda));
        when(redisRepository.existsById(any())).thenReturn(true);
        when(client.getStatus(anyString())).thenReturn(DocumentStatusResponse.builder().status(StatusMember.ABLE_TO_VOTE).build());
        when(voteRepository.existsByAgendaAndMemberCpf(any(), anyString())).thenReturn(false);
        when(voteMapper.toEntity(any(), any())).thenReturn(mock(VoteEntity.class));

        int threads = 5000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(i -> executor.submit(() -> {
            try {
                service.voteSession(agendaId, request);
            } finally {
                latch.countDown();
            }
        }));

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        verify(voteRepository, times(threads)).save(any());
    }
}
