package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.exception.BusinessException;
import br.com.sicred.assemblyvote.exception.NotFoundException;
import br.com.sicred.assemblyvote.mapper.VotingSessionMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionVoteService {

    @Value("${application.session-duration-sec.default}")
    private Long sessionDuration;

    private final VotingSessionRepository sessionRepository;
    private final AgendaRepository agendaRepository;
    private final VotingSessionMapper sessionMapper;
    private final SessionRedisRepository redisRepository;

    public VotingSessionResponse openSession(final UUID agendaId, final VotingSessionRequest request) {
        log.debug("Opening session - [agendaId={} and request={}]", agendaId, request);

        final var agenda = validateSession(agendaId);

        final var duration = resolveDuration(request);

        final var startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(duration);

        final var sessionEntity = sessionMapper.toEntity(request, agenda, startTime, endTime);
        final var sessionRedis = sessionMapper.toRedis(request, agendaId);

        final var sessionSaved = sessionRepository.save(sessionEntity);
        redisRepository.save(sessionRedis);

        return sessionMapper.toResponse(sessionSaved);
    }

    @Transactional
    public int closeExpiredSessions() {
        log.debug("Closing sessions older than {}", LocalDateTime.now());

        return sessionRepository.closeExpiredSessions(LocalDateTime.now());
    }

    private AgendaEntity validateSession(final UUID agendaId) {
        final var agenda = agendaRepository.findById(agendaId)
            .orElseThrow(() -> new NotFoundException("Agenda not found for id: " + agendaId));

        final var existsSessionOpened = redisRepository.existsById(agendaId);

        if (existsSessionOpened) {
            throw new BusinessException("Agenda already has an open session");
        }

        return agenda;
    }

    private Long resolveDuration(VotingSessionRequest request){
        if (request.durationSeconds() == null) {
            return sessionDuration;
        }

        return request.durationSeconds();
    }
}
