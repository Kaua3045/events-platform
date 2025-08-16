package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.ArrayList;
import java.util.Objects;

public class DefaultCreateCheckoutUseCase extends CreateCheckoutUseCase {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TracerWrapper tracerWrapper;
    private final TransactionManager transactionManager;

    public DefaultCreateCheckoutUseCase(
            final OrderRepository orderRepository,
            final TicketRepository ticketRepository,
            final TracerWrapper tracerWrapper,
            final TransactionManager transactionManager
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Override
    public CreateCheckoutOutput execute(final CreateCheckoutInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createCheckoutUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreateCheckoutUseCase.class);

                    final var aTickets = ctx.runInSpan(
                            "ticket.retrieves",
                            () -> input.items().stream().map(
                                    it -> this.ticketRepository.ticketOfId(it.ticketId())
                                            .orElseThrow(NotFoundException.with(Ticket.class, it.ticketId()))
                            ).toList()
                    );

                    final var orderItems = new ArrayList<OrderItem>();

                    for (var inputItem : input.items()) {
                        final var ticket = aTickets.stream()
                                .filter(t -> t.getId().value().toString().equals(inputItem.ticketId()))
                                .findFirst()
                                .orElseThrow(NotFoundException.with(Ticket.class, inputItem.ticketId()));

                        final int aAvailable = ticket.getQuantity() - ticket.getSold();
                        if (aAvailable < inputItem.quantity()) {
                            throw DomainException.with("Ticket %s is sold out or does not have enough quantity".formatted(ticket.getId()));
                        }

                        ticket.updateSold(ticket.getSold() + inputItem.quantity());

                        orderItems.add(OrderItem.newItem(
                                ticket.getEventId(),
                                ticket.getId(),
                                inputItem.quantity(),
                                ticket.getPrice()
                        ));
                    }

                    final var aOrder = Order.newOrder(
                            new UserID(ULID.fromString(input.userId())),
                            orderItems
                    );

                    // TODO Register OrderCreatedEvent

                    final var aTransactionResult = this.transactionManager.execute(() -> {
                        ctx.runInSpan(
                                "ticket.saveAll",
                                () -> this.ticketRepository.saveAll(aTickets)
                        );

                        String qrCodeUrl = null;
                        if (input.paymentDetails().method() == PaymentMethod.PIX) {
                            System.out.println("PaymentMethod is PIX, sending request to process");
                            qrCodeUrl = "HTTPS://Localhstoo128318u02:8080";
                        }

                        ctx.runInSpan(
                                "order.save",
                                () -> this.orderRepository.save(aOrder)
                        );

                        return CreateCheckoutOutput.from(
                                aOrder,
                                input.paymentDetails().method().name(),
                                qrCodeUrl
                        );
                    });

                    if (aTransactionResult.isFailure()) {
                        throw aTransactionResult.getException();
                    }

                    return aTransactionResult.getValue();
                }
        );
    }
}
