package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.ticket.req.CreateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.res.CreateTicketResponse;
import com.kaua.events.platform.infrastructure.ticket.res.ListTicketsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Ticket", description = "Ticket API")
@RequestMapping("/v1/tickets")
public interface TicketAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @IdempotencyKey
    @Operation(summary = "Create a new ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateTicketResponse> createTicket(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody CreateTicketRequest request
    );

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get all tickets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets successfully found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Pagination<ListTicketsResponse> listTickets(
            @RequestParam Map<String, String> filters,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "perPage", required = false, defaultValue = "10") int perPage,
            @RequestParam(name = "sort", required = false, defaultValue = "role") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate
    );
}
