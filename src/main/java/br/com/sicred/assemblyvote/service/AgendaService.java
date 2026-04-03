package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.exception.ServerErrorException;
import br.com.sicred.assemblyvote.mapper.AgendaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.Optional.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaService {

    private final AgendaRepository repository;
    private final AgendaMapper mapper;

    public CreateAgendaResponse createAgenda(final AgendaRequest request) {
        log.info("Creating an agenda...");

        return mapper.toResponse(
            of(repository.save(mapper.toEntity(request)))
                .orElseThrow(() -> new ServerErrorException("Error saving to database."))
        );
    }
}
