package br.com.sicred.assemblyvote.service;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.repository.AgendaRepository;
import br.com.sicred.assemblyvote.exception.ServerErrorException;
import br.com.sicred.assemblyvote.fixture.Fixture;
import br.com.sicred.assemblyvote.mapper.AgendaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaEntityServiceTest {

    @InjectMocks
    private AgendaService agendaService;
    
    @Mock
    private AgendaRepository repository;

    @Spy
    private AgendaMapper agendaMapper = Mappers.getMapper(AgendaMapper.class);

    private AgendaRequest mockRequest;
    private AgendaEntity mockEntity;

    @BeforeEach
    void setUp() {
        mockRequest = Fixture.make(AgendaRequest.class);
        mockEntity = Fixture.make(AgendaEntity.class);
    }

    @Test
    void shouldCreateAgenda() {
        when(agendaMapper.toEntity(any(AgendaRequest.class))).thenCallRealMethod();
        when(repository.save(any(AgendaEntity.class))).thenReturn(mockEntity);
        when(agendaMapper.toResponse(any(AgendaEntity.class))).thenCallRealMethod();

        final var result = agendaService.createAgenda(mockRequest);

        assertNotNull(result);
        assertEquals(mockEntity.getAgendaId(), result.agendaId());

        verify(agendaMapper, times(1)).toEntity(any(AgendaRequest.class));
        verify(repository, times(1)).save(any(AgendaEntity.class));
        verify(agendaMapper, times(1)).toResponse(any(AgendaEntity.class));
    }

    @Test
    void shouldntCreateAgendaWithErrorRepository() {
        when(agendaMapper.toEntity(any(AgendaRequest.class))).thenCallRealMethod();
        when(repository.save(any(AgendaEntity.class))).thenThrow(new ServerErrorException("Error saving to database."));

        assertThatThrownBy(() -> agendaService.createAgenda(mockRequest))
            .isInstanceOf(ServerErrorException.class)
            .hasMessageContaining("Error saving to database");

        verify(agendaMapper, times(1)).toEntity(any(AgendaRequest.class));
        verify(repository, times(1)).save(any(AgendaEntity.class));
        verifyNoMoreInteractions(agendaMapper);
    }
}
