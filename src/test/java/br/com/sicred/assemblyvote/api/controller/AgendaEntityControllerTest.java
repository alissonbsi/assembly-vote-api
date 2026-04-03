package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.api.handler.CustomControllerAdvice;
import br.com.sicred.assemblyvote.fixture.Fixture;
import br.com.sicred.assemblyvote.service.AgendaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendaController.class)
@Import(CustomControllerAdvice.class)
class AgendaEntityControllerTest {

    private static final String BASE_PATH = "/v1/agendas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgendaService agendaService;

    private AgendaRequest mockRequest;
    private CreateAgendaResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = Fixture.make(AgendaRequest.class);
        mockResponse = Fixture.make(CreateAgendaResponse.class);
    }

    @Test
    @WithMockUser
    void shouldCreateAgenda() throws Exception {
        when(agendaService.createAgenda(any(AgendaRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
            .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.agendaId").value(mockResponse.agendaId().toString()));

        verify(agendaService, times(1)).createAgenda(any(AgendaRequest.class));
    }

    @Test
    @WithMockUser
    void shouldntCreateAgendaWithBadRequest() throws Exception {
        final var mockedRequestError = AgendaRequest.builder().build();

        mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(mockedRequestError)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(agendaService);
    }
}
