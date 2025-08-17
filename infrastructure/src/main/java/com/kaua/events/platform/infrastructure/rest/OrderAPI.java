package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.orders.req.CreateCheckoutRequest;
import com.kaua.events.platform.infrastructure.orders.res.CreateCheckoutResponse;
import com.kaua.events.platform.infrastructure.orders.res.ListOrdersByUserIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Order", description = "Order API")
@RequestMapping("/v1/orders")
public interface OrderAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateCheckoutResponse> createCheckout(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody CreateCheckoutRequest request
    );

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get all orders for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders successfully found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Pagination<ListOrdersByUserIdResponse> listOrders(
            @RequestParam Map<String, String> filters,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "perPage", required = false, defaultValue = "10") int perPage,
            @RequestParam(name = "sort", required = false, defaultValue = "createdAt") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "desc") String direction,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @AuthenticationPrincipal AuthenticatedUser user
    );
}
