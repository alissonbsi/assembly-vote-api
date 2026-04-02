package br.com.sicred.assemblyvote.api.controller.dto.response;

import br.com.sicred.assemblyvote.domain.model.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VotingSessionResponse(UUID sessionId,
                                    String agendaTitle,
                                    SessionStatus status,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime) {
}
