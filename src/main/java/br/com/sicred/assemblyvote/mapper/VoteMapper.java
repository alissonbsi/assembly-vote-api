package br.com.sicred.assemblyvote.mapper;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface VoteMapper {

    @Mapping(target = "memberCpf", source = "request.cpf")
    @Mapping(target = "agenda", source = "agenda")
    VoteEntity toEntity(VoteRequest request, AgendaEntity agenda);
}
