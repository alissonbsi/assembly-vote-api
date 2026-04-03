package br.com.sicred.assemblyvote.performance;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.mapper.AgendaMapper;
import br.com.sicred.assemblyvote.service.AgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static br.com.sicred.assemblyvote.fixture.Fixture.make;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AgendaServicePerformanceTest {

    private AgendaRepository repository;
    private AgendaMapper mapper;
    private AgendaService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AgendaRepository.class);
        mapper = Mockito.mock(AgendaMapper.class);
        service = new AgendaService(repository, mapper);
    }

    @Test
    void testCreateAgendaPerformance() {
        AgendaRequest request = make(AgendaRequest.class);
        AgendaEntity entity = new AgendaEntity();
        CreateAgendaResponse response = make(CreateAgendaResponse.class);

        when(mapper.toEntity(any(AgendaRequest.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        long start = System.nanoTime();
        CreateAgendaResponse result = service.createAgenda(request);
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000;
        System.out.println("Execution time: " + durationMs + " ms");

        assertEquals(response, result);
        assertTrue(durationMs < 500, "Execution should be under 500ms");
    }
}
