package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.ticket.req.CreateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.req.UpdateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.res.CreateTicketResponse;
import com.kaua.events.platform.infrastructure.ticket.res.GetTicketByIdResponse;
import com.kaua.events.platform.infrastructure.ticket.res.ListTicketsResponse;
import com.kaua.events.platform.infrastructure.ticket.res.UpdateTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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

    @GetMapping(
            value = "/{ticketId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get a ticket by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket successfully found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<GetTicketByIdResponse> getTicketById(@PathVariable("ticketId") String ticketId);

    @PatchMapping(
            value = "/{ticketId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Update a ticket by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UpdateTicketResponse> updateTicket(
            @PathVariable("ticketId") String ticketId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody UpdateTicketRequest request
    );

    @DeleteMapping(value = "/{ticketId}")
    @Operation(summary = "Delete a ticket by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket was not found"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deleteTicket(
            @PathVariable("ticketId") String ticketId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    );
}
