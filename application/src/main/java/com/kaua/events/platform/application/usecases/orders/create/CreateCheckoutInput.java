package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPaymentDetailsInput;

import java.util.List;

public record CreateCheckoutInput(
        String userId,
        List<CreateCheckoutItemsInput> items,
        CreateCheckoutPaymentDetailsInput paymentDetails
) {

    public static CreateCheckoutInput with(
            final String userId,
            final List<CreateCheckoutItemsInput> items,
            final CreateCheckoutPaymentDetailsInput paymentDetails
    ) {
        return new CreateCheckoutInput(userId, items, paymentDetails);
    }
}
