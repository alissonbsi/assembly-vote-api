package br.com.sicred.assemblyvote.mapper;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AgendaMapper {

    AgendaEntity toEntity(AgendaRequest request);

    CreateAgendaResponse toResponse(AgendaEntity agendaEntity);
}
