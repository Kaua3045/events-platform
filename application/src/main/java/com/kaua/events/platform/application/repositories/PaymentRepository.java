package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.payments.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> paymentOfOrderId(String orderId);

    Payment save(Payment payment);
}
