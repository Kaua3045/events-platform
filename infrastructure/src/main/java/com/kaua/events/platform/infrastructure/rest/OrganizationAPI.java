package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.organizations.req.AddMemberToOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.req.CreateOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.res.AddMemberToOrganizationResponse;
import com.kaua.events.platform.infrastructure.organizations.res.CreateOrganizationResponse;
import com.kaua.events.platform.infrastructure.organizations.res.GetOrganizationByIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Organization", description = "Organization API")
@RequestMapping("/v1/organizations")
public interface OrganizationAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @IdempotencyKey
    @Operation(summary = "Create a new organization and owner user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization and Owner user created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateOrganizationResponse> createOrganization(@RequestBody CreateOrganizationRequest request);

    @PostMapping(
            path = "/add-member",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @IdempotencyKey
    @Operation(summary = "Add member to organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Added user member successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<AddMemberToOrganizationResponse> addMemberToOrganization(@RequestBody AddMemberToOrganizationRequest request);

    @GetMapping(
            path = "/{organizationId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get organization by it's identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization founded successfully"),
            @ApiResponse(responseCode = "404", description = "Organization was not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<GetOrganizationByIdResponse> getOrganizationById(@PathVariable String organizationId);

}
