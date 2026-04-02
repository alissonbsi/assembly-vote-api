package br.com.sicred.assemblyvote.api.controller;

import br.com.sicred.assemblyvote.api.controller.dto.request.AgendaRequest;
import br.com.sicred.assemblyvote.api.controller.dto.response.CreateAgendaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Agenda")
public interface AgendaApi {

    @Operation(summary = "Create an agenda.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created or validated biometry."),
        @ApiResponse(responseCode = "400", description = "Invalid data."),
        @ApiResponse(responseCode = "500", description = "Technical failure.")
    })
    CreateAgendaResponse createAgenda(
        @Parameter(description = "Data for creating an agenda.", required = true) AgendaRequest request);
}
