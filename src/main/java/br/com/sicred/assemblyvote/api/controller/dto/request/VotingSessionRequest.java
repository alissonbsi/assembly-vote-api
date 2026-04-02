package br.com.sicred.assemblyvote.api.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record VotingSessionRequest(@Positive
                                   @Schema(implementation = Long.class, description = "Duration must be in seconds - Default: 60", defaultValue = "60")
                                   Long durationSeconds) {
}
