package com.kaua.events.platform.application.usecases.orders.update.status;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;
import java.util.Optional;

public class DefaultUpdateOrderStatusUseCase extends UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    public DefaultUpdateOrderStatusUseCase(final OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    @Override
    public UpdateOrderStatusOutput execute(final UpdateOrderStatusInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(UpdateOrderStatusUseCase.class);

        final var aOrder = this.orderRepository.orderOfId(input.orderId())
                .orElseThrow(NotFoundException.with(Order.class, input.orderId()));

        final var aPaymentId = Optional.ofNullable(input.paymentId())
                .filter(it -> !it.isBlank())
                .map(it -> new PaymentID(ULID.fromString(it)))
                .orElseGet(() -> aOrder.getPaymentId().orElse(null));

        final var aOrderUpdated = switch (input.status()) {
            case "WAITING" -> aOrder.markAsPaymentPending(aPaymentId);
            case "APPROVED", "IDENTIFIED" -> aOrder.markAsPaymentApproved();
            case "PAID" -> aOrder.markAsPaid();
            case "FAILED" -> aOrder.markAsFailed();
            default -> throw DomainException.with("Invalid order status %s".formatted(input.status()));
        };

        this.orderRepository.save(aOrderUpdated);

        return UpdateOrderStatusOutput.from(aOrderUpdated);
    }
}
