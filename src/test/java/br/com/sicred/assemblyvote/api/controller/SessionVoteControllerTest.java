package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.api.handler.CustomControllerAdvice;
import br.com.sicred.assemblyvote.domain.model.VoteOption;
import br.com.sicred.assemblyvote.fixture.CpfFixture;
import br.com.sicred.assemblyvote.fixture.Fixture;
import br.com.sicred.assemblyvote.service.SessionVoteService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionVoteController.class)
@Import(CustomControllerAdvice.class)
class SessionVoteControllerTest {

    private static final String BASE_PATH = "/v1/session-vote/";
    private static final String CREATE_SESSION = "agenda/{agendaId}";
    private static final String SEND_VOTE = "agenda/{agendaId}/vote";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionVoteService sessionVoteService;

    private VotingSessionRequest mockSessionRequest;
    private VotingSessionResponse mockSessionResponse;
    private UUID agendaId = UUID.randomUUID();
    private VoteRequest voteRequest;

    @BeforeEach
    void setUp() {
        mockSessionRequest = Fixture.make(VotingSessionRequest.class);
        mockSessionResponse = Fixture.make(VotingSessionResponse.class);
        voteRequest = Fixture.make(VoteRequest.class);
    }

    @Test
    @WithMockUser
    void shouldOpenSession() throws Exception {
        when(sessionVoteService.openSession(any(UUID.class), any(VotingSessionRequest.class))).thenReturn(mockSessionResponse);

        mockMvc.perform(post(BASE_PATH + CREATE_SESSION, agendaId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
            .content(objectMapper.writeValueAsString(mockSessionRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sessionId").value(mockSessionResponse.sessionId().toString()))
            .andExpect(jsonPath("$.agendaTitle").value(mockSessionResponse.agendaTitle()))
            .andExpect(jsonPath("$.status").value(mockSessionResponse.status().name()))
            .andExpect(jsonPath("$.startTime").value(mockSessionResponse.startTime().toString()));

        verify(sessionVoteService, times(1)).openSession(any(UUID.class), any(VotingSessionRequest.class));
    }

    @Test
    @WithMockUser
    void shouldntOpenSessionWithInternalServerError() throws Exception {
        final var mockedRequestError = VotingSessionRequest.builder().durationSeconds(600L).build();

        mockMvc.perform(post(BASE_PATH + CREATE_SESSION, "XPTO")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(mockedRequestError)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        verifyNoInteractions(sessionVoteService);
    }

    @Test
    @WithMockUser
    void shouldReceiveVote() throws Exception {
        doNothing().when(sessionVoteService).voteSession(any(UUID.class), any(VoteRequest.class));

        final var voteRequest = VoteRequest.builder().vote(VoteOption.NO).cpf(CpfFixture.cpfValid()).build();

        mockMvc.perform(post(BASE_PATH + SEND_VOTE, agendaId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(voteRequest)))
            .andExpect(status().isAccepted());


        verify(sessionVoteService, times(1)).voteSession(any(UUID.class), any(VoteRequest.class));
    }

    @Test
    @WithMockUser
    void shouldntReceiveVoteWithBadRequestError() throws Exception {
        final var voteRequest = VoteRequest.builder().vote(VoteOption.NO).build();

        mockMvc.perform(post(BASE_PATH + SEND_VOTE, agendaId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(voteRequest)))
            .andExpect(status().isBadRequest());


        verifyNoInteractions(sessionVoteService);
    }
}
