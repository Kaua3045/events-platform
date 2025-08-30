package com.kaua.events.platform.infrastructure.configurations.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutCreditCardPaymentDetails;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPaymentDetailsInput;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPixPaymentDetails;

import java.io.IOException;

public class CreatePaymentDetailsDeserializer extends StdDeserializer<CreateCheckoutPaymentDetailsInput> {

    public CreatePaymentDetailsDeserializer() {
        super(CreateCheckoutPaymentDetailsInput.class);
    }

    @Override
    public CreateCheckoutPaymentDetailsInput deserialize(
            final JsonParser p,
            final DeserializationContext ctxt
    ) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String method = node.get("method").asText();
        if ("PIX".equalsIgnoreCase(method)) {
            return new CreateCheckoutPixPaymentDetails();
        } else if ("CREDIT_CARD".equalsIgnoreCase(method)) {
            return new CreateCheckoutCreditCardPaymentDetails();
        }
        throw new IllegalArgumentException("Unknown payment method: " + method);
    }
}
