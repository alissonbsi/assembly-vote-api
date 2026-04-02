package br.com.sicred.assemblyvote.mapper;

import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.cache.model.SessionRedis;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VotingSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper
public interface VotingSessionMapper {

    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "agenda", source = "agenda")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    VotingSessionEntity toEntity(VotingSessionRequest request,
                                 AgendaEntity agenda,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime);

    @Mapping(source = "agenda.title", target = "agendaTitle")
    VotingSessionResponse toResponse(VotingSessionEntity entity);

    @Mapping(target = "agendaId", source = "agendaId")
    @Mapping(target = "ttl", source = "request.durationSeconds")
    SessionRedis toRedis(VotingSessionRequest request, UUID agendaId);
}
