package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.cache.model.SessionRedis;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VotingSessionEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.exception.BusinessException;
import br.com.sicred.assemblyvote.exception.NotFoundException;
import br.com.sicred.assemblyvote.fixture.Fixture;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SessionVoteServiceTest {

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

    private VotingSessionRequest mockRequest;
    private AgendaEntity mockAgenda;
    private VotingSessionEntity mockSessionEntity;

    @BeforeEach
    void setUp() {
        mockRequest = Fixture.make(VotingSessionRequest.class);
        mockAgenda = Fixture.make(AgendaEntity.class);
        mockSessionEntity = Fixture.make(VotingSessionEntity.class);

        setField(sessionVoteService, "sessionDuration", 60L);
    }

    @Test
    void shouldOpenSessionSuccessfully() {
        when(agendaRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockAgenda));
        when(redisRepository.existsById(any(UUID.class))).thenReturn(false);
        when(sessionRepository.save(any(VotingSessionEntity.class))).thenReturn(mockSessionEntity);
        when(sessionMapper.toRedis(any(VotingSessionRequest.class), any(UUID.class))).thenReturn(Fixture.make(SessionRedis.class));

        final var response = sessionVoteService.openSession(UUID.randomUUID(), mockRequest);

        assertNotNull(response);
        verify(agendaRepository, times(1)).findById(any(UUID.class));
        verify(redisRepository, times(1)).existsById(any(UUID.class));
        verify(sessionRepository, times(1)).save(any(VotingSessionEntity.class));
        verify(sessionMapper, times(1)).toRedis(any(VotingSessionRequest.class), any(UUID.class));
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
}
