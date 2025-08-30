package com.kaua.events.platform.infrastructure.configurations.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutCreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.PaymentDetails;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;

import java.io.IOException;

public class PaymentDetailsSerializer extends StdSerializer<PaymentDetails> {

    public PaymentDetailsSerializer() {
        super(PaymentDetails.class);
    }

    @Override
    public void serialize(PaymentDetails value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("method", value.method().name());

        if (value instanceof PixPaymentDetails pix) {
            gen.writeNumberField("amount", pix.amount());
        } else if (value instanceof CreditCardPaymentDetails creditCard) {
            gen.writeNumberField("amount", creditCard.amount());
            gen.writeStringField("name", creditCard.name());
            gen.writeStringField("cpf", creditCard.cpf());
            gen.writeStringField("email", creditCard.email());
            gen.writeStringField("payment_token", creditCard.paymentToken());
            gen.writeNumberField("installments", creditCard.installments());
        }

        gen.writeEndObject();
    }
}
