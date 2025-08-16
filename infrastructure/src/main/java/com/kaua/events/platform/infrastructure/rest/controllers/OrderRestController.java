package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutUseCase;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.orders.req.CreateCheckoutRequest;
import com.kaua.events.platform.infrastructure.orders.res.CreateCheckoutResponse;
import com.kaua.events.platform.infrastructure.rest.OrderAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OrderRestController implements OrderAPI {

    private final CreateCheckoutUseCase createCheckoutUseCase;

    public OrderRestController(
            final CreateCheckoutUseCase createCheckoutUseCase
    ) {
        this.createCheckoutUseCase = Objects.requireNonNull(createCheckoutUseCase);
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
}
