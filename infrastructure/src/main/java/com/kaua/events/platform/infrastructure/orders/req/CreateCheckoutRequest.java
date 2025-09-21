package com.kaua.events.platform.infrastructure.orders.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutInput;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPaymentDetailsInput;

import java.util.List;

public record CreateCheckoutRequest(
        @JsonProperty("items") List<CreateCheckoutItemsRequest> items,
        @JsonProperty("payment_details") CreateCheckoutPaymentDetailsInput paymentDetails
) {

    public CreateCheckoutInput toInput(final String userId) {
        return new CreateCheckoutInput(
                userId,
                items().stream().map(CreateCheckoutItemsRequest::toInput).toList(),
                paymentDetails()
        );
    }
}
