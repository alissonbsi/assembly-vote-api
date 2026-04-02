package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import br.com.sicred.assemblyvote.api.handler.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "agenda")
public interface AgendaApi {

    @Operation(summary = "Create an agenda.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Agenda created"),
        @ApiResponse(responseCode = "400", description = "Invalid data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Technical failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    CreateAgendaResponse createAgenda(
        @Parameter(description = "Data for creating an agenda.", required = true) AgendaRequest request);
}
