package com.kaua.events.platform.infrastructure.configurations.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.PaymentDetails;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;

import java.io.IOException;
import java.math.BigDecimal;

public class PaymentDetailsDeserializer extends StdDeserializer<PaymentDetails> {

    public PaymentDetailsDeserializer() {
        super(PaymentDetails.class);
    }

    @Override
    public PaymentDetails deserialize(
            final JsonParser p,
            final DeserializationContext ctxt
    ) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String method = node.get("method").asText();
        if ("PIX".equalsIgnoreCase(method)) {
            return new PixPaymentDetails(
                    new BigDecimal(node.get("amount").asText()),
                    node.get("qr_code").asText(null),
                    node.get("qr_code_image_url").asText(null),
                    node.get("expires_in").asInt(0)
            );
        } else if ("CREDIT_CARD".equalsIgnoreCase(method)) {
            return new CreditCardPaymentDetails(
                    new BigDecimal(node.get("amount").asText()),
                    node.get("payment_token").asText(),
                    node.get("installments").asInt(),
                    node.get("user_id").asText()
            );
        }
        throw new IllegalArgumentException("Unknown payment method: " + method);
    }
}
