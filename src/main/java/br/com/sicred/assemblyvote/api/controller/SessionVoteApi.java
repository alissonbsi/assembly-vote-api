package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.VoteRequest;
import br.com.sicred.assemblyvote.api.controller.dto.request.VotingSessionRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.VotingSessionResponse;
import br.com.sicred.assemblyvote.api.handler.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import java.util.UUID;

@Tag(name = "session-vote")
public interface SessionVoteApi {

    @Operation(summary = "Open voting session on an agenda.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Session opened successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Agenda not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Business Error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Technical failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
    })
    VotingSessionResponse openSession(@Parameter(description = "Data to open a voting session.", required = true) VotingSessionRequest request,
                                      @Parameter(description = "Agenda ID", required = true) UUID agendaId);

    @Operation(summary = "Receives member vote.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "VoteEntity accepted"),
        @ApiResponse(responseCode = "400", description = "Invalid data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Agenda not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Business Error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Technical failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
    })
    void receiveVote(@Parameter(required = true) VoteRequest request,
                     @Parameter(description = "Agenda ID", required = true) UUID agendaId);
}
