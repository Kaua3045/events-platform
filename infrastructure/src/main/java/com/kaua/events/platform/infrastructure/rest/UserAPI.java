package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.users.req.CreateUserRequest;
import com.kaua.events.platform.infrastructure.users.req.UpdateUserDocumentRequest;
import com.kaua.events.platform.infrastructure.users.res.CreateUserResponse;
import com.kaua.events.platform.infrastructure.users.res.GetUserByIdResponse;
import com.kaua.events.platform.infrastructure.users.res.UpdateUserDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "User API")
@RequestMapping("/v1/users")
public interface UserAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request);

    @GetMapping(
            path = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User identifier was not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<GetUserByIdResponse> getMe(@AuthenticationPrincipal AuthenticatedUser user);

    @PatchMapping(
            path = "/update/document",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Update a user document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User document updated successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<UpdateUserDocumentResponse> updateDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody UpdateUserDocumentRequest request
    );
}
