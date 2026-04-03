package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.client.ValidateMemberClient;
import br.com.sicred.assemblyvote.client.response.DocumentStatusResponse;
import br.com.sicred.assemblyvote.client.response.StatusMember;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VotingSessionEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VoteRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.exception.BusinessException;
import br.com.sicred.assemblyvote.exception.NotFoundException;
import br.com.sicred.assemblyvote.fixture.Fixture;
import br.com.sicred.assemblyvote.mapper.VoteMapper;
import br.com.sicred.assemblyvote.mapper.VotingSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SessionVoteEntityServiceTest {

    @InjectMocks
    private SessionVoteService sessionVoteService;

    @Mock
    private VotingSessionRepository sessionRepository;

    @Mock
    private AgendaRepository agendaRepository;

    @Spy
    private VotingSessionMapper sessionMapper = Mappers.getMapper(VotingSessionMapper.class);

    @Mock
    private SessionRedisRepository redisRepository;

    @Mock
    private VoteRepository voteRepository;

    @Spy
    private VoteMapper voteMapper = Mappers.getMapper(VoteMapper.class);

    @Mock
    private ValidateMemberClient client;

    private VotingSessionRequest mockRequest;
    private AgendaEntity mockAgenda;
    private VotingSessionEntity mockSessionEntity;
    private VoteRequest mockVoteRequest;

    @BeforeEach
    void setUp() {
        mockRequest = Fixture.make(VotingSessionRequest.class);
        mockAgenda = Fixture.make(AgendaEntity.class);
        mockSessionEntity = Fixture.make(VotingSessionEntity.class);
        mockVoteRequest = Fixture.make(VoteRequest.class);

        setField(sessionVoteService, "sessionDuration", 60L);
    }

    @Test
    void shouldOpenSessionSuccessfully() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(redisRepository.existsById(any(UUID.class))).thenReturn(false);
        when(sessionRepository.save(any(VotingSessionEntity.class))).thenReturn(mockSessionEntity);

        final var response = sessionVoteService.openSession(UUID.randomUUID(), mockRequest);

        assertNotNull(response);
        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verify(sessionRepository, times(1)).save(any(VotingSessionEntity.class));
        verify(sessionMapper, times(1)).toRedis(anyLong(), any(UUID.class));
        verify(sessionMapper, times(1)).toResponse(any(VotingSessionEntity.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAgendaNotExists() {
        final var agendaId = UUID.randomUUID();
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionVoteService.openSession(agendaId, mockRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Agenda not found for id: " + agendaId);

        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(redisRepository, sessionRepository, sessionMapper);
    }

    @Test
    void shouldThrowBusinessExceptionWhenSessionAlreadyExistsInRedis() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(redisRepository.existsById(any(UUID.class))).thenReturn(true);

        assertThatThrownBy(() -> sessionVoteService.openSession(UUID.randomUUID(), mockRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Agenda already has an open session");

        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(sessionRepository, sessionMapper);
    }

    @Test
    void shouldCloseExpiredSessionsSuccessfully() {
        when(sessionRepository.closeExpiredSessions(any(LocalDateTime.class))).thenReturn(3);

        final int closedCount = sessionVoteService.closeExpiredSessions();

        assertEquals(3, closedCount);
        verify(sessionRepository, times(1)).closeExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    void shouldVoteSuccessfully() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(client.getStatus(anyString())).thenReturn(DocumentStatusResponse.builder().status(StatusMember.ABLE_TO_VOTE).build());
        when(redisRepository.existsById(any(UUID.class))).thenReturn(true);

        sessionVoteService.voteSession(UUID.randomUUID(), mockVoteRequest);

        verify(voteRepository, times(1)).save(any());
        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verify(client, times(1)).getStatus(anyString());
        verify(voteMapper, times(1)).toEntity(any(VoteRequest.class), any(AgendaEntity.class));
    }

    @Test
    void shouldThrowBusinessExceptionIfMemberCannotVote() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(client.getStatus(anyString())).thenReturn(DocumentStatusResponse.builder().status(StatusMember.UNABLE_TO_VOTE).build());
        when(redisRepository.existsById(any(UUID.class))).thenReturn(true);

        assertThatThrownBy(() -> sessionVoteService.voteSession(UUID.randomUUID(), mockVoteRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Member unabled to vote!");

        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verify(client, times(1)).getStatus(anyString());
        verifyNoInteractions(voteMapper, voteRepository);
    }

    @Test
    void shouldThrowBusinessExceptionIfSessionNotOpened() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(redisRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThatThrownBy(() -> sessionVoteService.voteSession(UUID.randomUUID(), mockVoteRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Agenda is not opened");

        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verifyNoInteractions(voteMapper, voteRepository, client);
    }

    @Test
    void shouldThrowBusinessExceptionIfUserAlreadyVote() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(client.getStatus(anyString())).thenReturn(DocumentStatusResponse.builder().status(StatusMember.ABLE_TO_VOTE).build());
        when(redisRepository.existsById(any(UUID.class))).thenReturn(true);
        when(voteRepository.existsByAgendaAndMemberCpf(any(AgendaEntity.class), any())).thenReturn(true);

        assertThatThrownBy(() -> sessionVoteService.voteSession(UUID.randomUUID(), mockVoteRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("User has already voted on this agenda");

        verify(voteRepository, times(1)).existsByAgendaAndMemberCpf(any(AgendaEntity.class), any());
        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verify(client, times(1)).getStatus(anyString());
        verifyNoInteractions(voteMapper);
        verifyNoMoreInteractions(voteRepository);
    }
}
