package br.com.sicred.assemblyvote.api.controller.dto.request;

import br.com.sicred.assemblyvote.domain.model.VoteOption;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VoteRequest(@NotNull String cpf,
                          @NotNull VoteOption vote) {
}
