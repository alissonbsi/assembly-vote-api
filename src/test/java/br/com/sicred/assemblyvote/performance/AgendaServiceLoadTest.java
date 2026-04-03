package br.com.sicred.assemblyvote.performance;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.mapper.AgendaMapper;
import br.com.sicred.assemblyvote.service.AgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static br.com.sicred.assemblyvote.fixture.Fixture.make;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgendaServiceLoadTest {

    private AgendaRepository repository;
    private AgendaMapper mapper;
    private AgendaService service;

    @BeforeEach
    void setUp() {
        repository = mock(AgendaRepository.class);
        mapper = mock(AgendaMapper.class);
        service = new AgendaService(repository, mapper);
    }

    @Test
    void testCreateAgendaUnderLoad() throws InterruptedException {
        AgendaRequest request = make(AgendaRequest.class);
        var entity = new AgendaEntity();
        var response = make(CreateAgendaResponse.class);

        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        /**
         * Número de usuários
         */
        int threads = 5000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(i -> executor.submit(() -> {
            try {
                service.createAgenda(request);
            } finally {
                latch.countDown();
            }
        }));

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        verify(repository, times(threads)).save(entity);
        verify(mapper, times(threads)).toResponse(entity);
    }
}
