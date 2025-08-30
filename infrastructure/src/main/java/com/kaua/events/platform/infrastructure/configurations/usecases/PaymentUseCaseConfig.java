package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentUseCase;
import com.kaua.events.platform.application.usecases.payments.create.DefaultCreatePaymentUseCase;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PaymentUseCaseConfig {

    @Bean
    public CreatePaymentUseCase createPaymentUseCase(
            final PaymentRepository paymentRepository,
            final PaymentGateway paymentGateway,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultCreatePaymentUseCase(
                paymentRepository,
                paymentGateway,
                tracerWrapper
        );
    }
}
