package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.eventmanagement.req.CreateEventRequest;
import com.kaua.events.platform.infrastructure.eventmanagement.req.UpdateEventRequest;
import com.kaua.events.platform.infrastructure.eventmanagement.res.CreateEventResponse;
import com.kaua.events.platform.infrastructure.eventmanagement.res.GetEventByIdResponse;
import com.kaua.events.platform.infrastructure.eventmanagement.res.ListEventsResponse;
import com.kaua.events.platform.infrastructure.eventmanagement.res.UpdateEventResponse;
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

@Tag(name = "Event", description = "Event API")
@RequestMapping("/v1/events")
public interface EventAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest request);

    // TODO o spring nao lida bem com receber Map<String, String> como parametro
    // O que seria ideal, seria remover os outros e passar tudo dentro do map
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get all events")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events successfully found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Pagination<ListEventsResponse> listEvents(
            @RequestParam Map<String, String> filters,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "perPage", required = false, defaultValue = "10") int perPage,
            @RequestParam(name = "sort", required = false, defaultValue = "createdAt") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate
    );

    @GetMapping(
            value = "/{eventId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get event by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event successfully found"),
            @ApiResponse(responseCode = "404", description = "Event was not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<GetEventByIdResponse> getEventById(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable("eventId") String eventId);

    @PatchMapping(
            value = "/{eventId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Update event by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UpdateEventResponse> updateEvent(@PathVariable("eventId") String eventId, @RequestBody UpdateEventRequest request, @AuthenticationPrincipal AuthenticatedUser user);

    @DeleteMapping(
            value = "/{eventId}"
    )
    @Operation(summary = "Soft delete an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event successfully deleted"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void softDeleteEvent(
            @PathVariable("eventId") String eventId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    );
}
