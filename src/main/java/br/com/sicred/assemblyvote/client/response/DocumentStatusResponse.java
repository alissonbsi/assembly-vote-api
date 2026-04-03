package br.com.sicred.assemblyvote.client.response;

import lombok.Builder;

@Builder
public record DocumentStatusResponse(StatusMember status) {
}
