package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;

// TODO em vez de todas as infos do user, deviamos salvar so o id e brand, last4, expMont e expYear
// e referenciar payment details no payment, mover tudo de pix do pix details
// e salvar tudo isso no payment_details table, mas pra isso acontecer
// precisamos primeiro adicionar cpf e phoneNumber no user
// depois refatorar tudo de checkout pra receber o authenticatedUserId
// dai passar pro evento, refatorar o payment pra receber os details
// depois salvar no db
public record CreditCardPaymentDetails(BigDecimal amount, String paymentToken, int installments,
                                       String userId) implements PaymentDetails {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }
}
