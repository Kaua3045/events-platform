package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdUseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.Period;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.orders.req.CreateCheckoutRequest;
import com.kaua.events.platform.infrastructure.orders.res.CreateCheckoutResponse;
import com.kaua.events.platform.infrastructure.orders.res.ListOrdersByUserIdResponse;
import com.kaua.events.platform.infrastructure.rest.OrderAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

@RestController
public class OrderRestController implements OrderAPI {

    private final CreateCheckoutUseCase createCheckoutUseCase;
    private final ListOrdersByUserIdUseCase listOrdersByUserIdUseCase;

    public OrderRestController(
            final CreateCheckoutUseCase createCheckoutUseCase,
            final ListOrdersByUserIdUseCase listOrdersByUserIdUseCase
    ) {
        this.createCheckoutUseCase = Objects.requireNonNull(createCheckoutUseCase);
        this.listOrdersByUserIdUseCase = Objects.requireNonNull(listOrdersByUserIdUseCase);
    }

    @IdempotencyKey
    @Override
    public ResponseEntity<CreateCheckoutResponse> createCheckout(
            final AuthenticatedUser authenticatedUser,
            final CreateCheckoutRequest request
    ) {
        final var aInput = request.toInput(authenticatedUser.id());

        final var aOutput = this.createCheckoutUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateCheckoutResponse.from(aOutput));
    }

    @Override
    public Pagination<ListOrdersByUserIdResponse> listOrders(
            final Map<String, String> filters,
            final String search,
            final int page,
            final int perPage,
            final String sort,
            final String direction,
            final String startDate,
            final String endDate,
            final AuthenticatedUser user
    ) {
        filters.put("filters.userId", user.id());
        final var aQuery = SearchQuery.newSearchQuery(
                page,
                perPage,
                search,
                sort,
                direction,
                new Period(Period.startValidate(
                        startDate,
                        30,
                        ChronoUnit.DAYS
                ), Period.endValidate(
                        endDate,
                        30,
                        ChronoUnit.DAYS
                )),
                filters
        );

        final var aOutput = this.listOrdersByUserIdUseCase.execute(aQuery);

        return aOutput.map(ListOrdersByUserIdResponse::from);
    }
}
