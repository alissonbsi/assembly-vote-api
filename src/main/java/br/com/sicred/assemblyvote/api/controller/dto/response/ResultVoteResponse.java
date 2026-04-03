package br.com.sicred.assemblyvote.api.controller.dto.response;

import br.com.sicred.assemblyvote.domain.model.Result;
import br.com.sicred.assemblyvote.domain.model.SessionStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ResultVoteResponse(UUID agendaId,
                                 UUID sessionId,
                                 String title,
                                 SessionStatus status,
                                 Result result) {
}
