package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.payments.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);
}
