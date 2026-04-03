package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.client.ValidateMemberClient;
import br.com.sicred.assemblyvote.client.response.StatusMember;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VoteRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.exception.BusinessException;
import br.com.sicred.assemblyvote.exception.NotFoundException;
import br.com.sicred.assemblyvote.exception.ServerErrorException;
import br.com.sicred.assemblyvote.mapper.VoteMapper;
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
    private final VoteRepository voteRepository;
    private final VoteMapper voteMapper;
    private final ValidateMemberClient client;

    public VotingSessionResponse openSession(final UUID agendaId, final VotingSessionRequest request) {
        log.debug("Opening session - [agendaId={} and request={}]", agendaId, request);

        final var agenda = fetchAgenda(agendaId);

        existsSessionOpened(agendaId);

        final var duration = resolveDuration(request);

        final var startTime = LocalDateTime.now();
        final var endTime = startTime.plusSeconds(duration);

        final var sessionEntity = sessionMapper.toEntity(agenda, startTime, endTime);
        final var sessionRedis = sessionMapper.toRedis(request.durationSeconds(), agendaId);

        final var sessionSaved = sessionRepository.save(sessionEntity);
        redisRepository.save(sessionRedis);

        return sessionMapper.toResponse(sessionSaved);
    }

    @Transactional
    public Integer closeExpiredSessions() {
        log.debug("Closing sessions older than {}", LocalDateTime.now());

        return sessionRepository.closeExpiredSessions(LocalDateTime.now());
    }

    @Transactional
    public void voteSession(final UUID agendaId, final VoteRequest request) {
        log.info("VoteSession - [agendaId={}, request={}]", agendaId, request);

        final var agenda = fetchAgenda(agendaId);

        validateVoteMember(agenda, request.cpf());

        try {
            voteRepository.save(voteMapper.toEntity(request, agenda));
        } catch (final Exception e) {
            log.error("Error when voting for CPF {} - {}", request.cpf(), e.getMessage());
            throw new ServerErrorException(e.getMessage());
        }

        log.info("Vote successfully counted - [agendaId={}, request={}]", agendaId, request);
    }

    private void existsSessionOpened(final UUID agendaId) {
        final var existsSessionOpened = redisRepository.existsById(agendaId);

        if (existsSessionOpened) {
            throw new BusinessException("Agenda already has an open session");
        }
    }

    private void validateVoteMember(final AgendaEntity agenda, final String cpf) {
        if (!isSessionOpened(agenda.getAgendaId())) {
            log.warn("Agenda is not opened - [agendaId={}]", agenda.getAgendaId());
            throw new BusinessException("Agenda is not opened");
        }

        final var memberStatus = client.getStatus(cpf);

        if(StatusMember.UNABLE_TO_VOTE == memberStatus.status()) {
            log.warn("Member unabled to vote - [agendaId={}, cpd={}]", agenda.getAgendaId(), cpf);
            throw new BusinessException("Member unabled to vote!");
        }

        if (hasUserAlreadyVote(agenda, cpf)) {
            log.warn("Agenda {} is already voted for user cpf {}", agenda.getAgendaId(), cpf);
            throw new BusinessException("User has already voted on this agenda");
        }
    }

    private boolean isSessionOpened(final UUID agendaId) {
        return redisRepository.existsById(agendaId);
    }

    private boolean hasUserAlreadyVote(final AgendaEntity agenda, final String cpf) {
        return voteRepository.existsByAgendaAndMemberCpf(agenda, cpf);
    }

    private Long resolveDuration(VotingSessionRequest request){
        if (request.durationSeconds() == null) {
            return sessionDuration;
        }

        return request.durationSeconds();
    }

    private AgendaEntity fetchAgenda(final UUID agendaId) {
        return agendaRepository.findById(agendaId)
            .orElseThrow(() -> new NotFoundException("Agenda not found for id: " + agendaId));
    }
}
