package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.ResultVoteResponse;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.cache.repository.SessionRedisRepository;
import br.com.sicred.assemblyvote.client.ValidateMemberClient;
import br.com.sicred.assemblyvote.client.response.StatusMember;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.Result;
import br.com.sicred.assemblyvote.domain.model.VoteOption;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.domain.repository.VoteCountProjection;
import br.com.sicred.assemblyvote.domain.repository.VoteRepository;
import br.com.sicred.assemblyvote.domain.repository.VotingSessionRepository;
import br.com.sicred.assemblyvote.exception.BusinessException;
import br.com.sicred.assemblyvote.exception.NotFoundException;
import br.com.sicred.assemblyvote.mapper.VoteMapper;
import br.com.sicred.assemblyvote.mapper.VotingSessionMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

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
        log.debug("Opening session [agendaId={}]", agendaId);

        final var agenda = fetchAgenda(agendaId);

        ensureNoOpenSession(agendaId);

        final var duration = resolveDuration(request);
        final var now = LocalDateTime.now();

        final var session = sessionMapper.toEntity(agenda, now, now.plusSeconds(duration));
        final var redisSession = sessionMapper.toRedis(duration, agendaId);

        sessionRepository.save(session);
        redisRepository.save(redisSession);

        return sessionMapper.toResponse(session);
    }

    @Transactional
    public Integer closeExpiredSessions() {
        final var now = LocalDateTime.now();
        log.debug("Closing expired sessions [now={}]", now);

        return sessionRepository.closeExpiredSessions(now);
    }

    @Transactional
    public void voteSession(final UUID agendaId, final VoteRequest request) {
        log.info("Voting [agendaId={}, cpf={}]", agendaId, request.cpf());

        final var agenda = fetchAgenda(agendaId);

        validateVoting(agenda, request.cpf());

        voteRepository.save(voteMapper.toEntity(request, agenda));

        log.info("Vote registered [agendaId={}, cpf={}]", agendaId, request.cpf());
    }

    public ResultVoteResponse resultVotation(final UUID agendaId) {
        log.info("Fetching result [agendaId={}]", agendaId);

        validateSessionClosed(agendaId);

        final var agenda = fetchAgenda(agendaId);

        final var voteMap = voteRepository.countVotesByAgenda(agenda)
            .stream()
            .collect(Collectors.toMap(
                VoteCountProjection::getVote,
                VoteCountProjection::getTotal
            ));

        final var result = calculateResult(
            voteMap.getOrDefault(VoteOption.YES, 0L),
            voteMap.getOrDefault(VoteOption.NO, 0L)
        );

        return buildResponse(agenda, agendaId, result);
    }

    private void ensureNoOpenSession(final UUID agendaId) {
        if (isSessionOpened(agendaId)) {
            throw new BusinessException("Agenda already has an open session");
        }
    }

    private void validateVoting(final AgendaEntity agenda, final String cpf) {
        validateSessionOpen(agenda.getAgendaId());
        validateMemberStatus(agenda, cpf);
        validateDuplicateVote(agenda, cpf);
    }

    private void validateSessionOpen(final UUID agendaId) {
        if (!isSessionOpened(agendaId)) {
            log.warn("Session not open [agendaId={}]", agendaId);
            throw new BusinessException("Session is not open");
        }
    }

    private void validateSessionClosed(final UUID agendaId) {
        if (isSessionOpened(agendaId)) {
            log.warn("Result requested before session closed [agendaId={}]", agendaId);
            throw new BusinessException("Session is still open");
        }
    }

    private void validateMemberStatus(final AgendaEntity agenda, final String cpf) {
        final var status = client.getStatus(cpf);

        if (StatusMember.UNABLE_TO_VOTE == status.status()) {
            log.warn("Member unable to vote [agendaId={}, cpf={}]", agenda.getAgendaId(), cpf);
            throw new BusinessException("Member unable to vote");
        }
    }

    private void validateDuplicateVote(final AgendaEntity agenda, final String cpf) {
        if (voteRepository.existsByAgendaAndMemberCpf(agenda, cpf)) {
            log.warn("Duplicate vote [agendaId={}, cpf={}]", agenda.getAgendaId(), cpf);
            throw new BusinessException("User has already voted");
        }
    }

    private Result calculateResult(long yes, long no) {
        int comparison = Long.compare(yes, no);

        return switch (comparison) {
            case 1 -> Result.APPROVED;
            case -1 -> Result.REJECTED;
            default -> Result.TIED;
        };
    }

    private ResultVoteResponse buildResponse(AgendaEntity agenda, UUID agendaId, Result result) {
        return ResultVoteResponse.builder()
            .title(agenda.getTitle())
            .agendaId(agendaId)
            .sessionId(agenda.getSession().getSessionId())
            .status(agenda.getSession().getStatus())
            .result(result)
            .build();
    }

    private boolean isSessionOpened(final UUID agendaId) {
        return redisRepository.existsById(agendaId);
    }

    private Long resolveDuration(VotingSessionRequest request) {
        return request.durationSeconds() != null
            ? request.durationSeconds()
            : sessionDuration;
    }

    private AgendaEntity fetchAgenda(final UUID agendaId) {
        return agendaRepository.findById(agendaId)
            .orElseThrow(() -> new NotFoundException("Agenda not found for id: " + agendaId));
    }
}
