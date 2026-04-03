package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.service.AgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/agendas")
public class AgendaController implements AgendaApi {

    private final AgendaService agendaService;

    @Override
    @PostMapping
    @ResponseStatus(CREATED)
    public CreateAgendaResponse createAgenda(@RequestBody @Valid final AgendaRequest request) {
        return agendaService.createAgenda(request);
    }
}
