package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.ResultVoteResponse;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.service.SessionVoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/session-vote/")
public class SessionVoteController implements SessionVoteApi {

    private final SessionVoteService sessionService;

    @Override
    @PostMapping("agenda/{agendaId}")
    @ResponseStatus(CREATED)
    public VotingSessionResponse openSession(@Valid @RequestBody final VotingSessionRequest request,
                                             @PathVariable(value = "agendaId") final UUID agendaId) {
        return sessionService.openSession(agendaId, request);
    }

    @Override
    @PostMapping("agenda/{agendaId}/vote")
    @ResponseStatus(ACCEPTED)
    public void receiveVote(@Valid @RequestBody final VoteRequest request,
                            @PathVariable(value = "agendaId") final UUID agendaId) {
        sessionService.voteSession(agendaId, request);
    }

    @Override
    @GetMapping("agenda/{agendaId}/vote/result")
    @ResponseStatus(OK)
    public ResultVoteResponse resultVotation(@PathVariable(value = "agendaId") final UUID agendaId) {
        return sessionService.resultVotation(agendaId);
    }
}
