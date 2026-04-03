package br.com.sicred.assemblyvote.api.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AgendaRequest(@NotBlank String title,
                            @NotBlank String description) {
}
