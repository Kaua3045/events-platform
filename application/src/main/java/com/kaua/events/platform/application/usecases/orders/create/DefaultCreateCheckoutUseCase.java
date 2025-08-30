package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutCreditCardPaymentDetails;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentInput;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentUseCase;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.domain.orders.events.OrderCreatedEvent;
import com.kaua.events.platform.domain.payments.*;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultCreateCheckoutUseCase extends CreateCheckoutUseCase {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TracerWrapper tracerWrapper;
    private final TransactionManager transactionManager;
    private final CreatePaymentUseCase createPaymentUseCase;

    public DefaultCreateCheckoutUseCase(
            final OrderRepository orderRepository,
            final TicketRepository ticketRepository,
            final TracerWrapper tracerWrapper,
            final TransactionManager transactionManager,
            final CreatePaymentUseCase createPaymentUseCase
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
        this.transactionManager = Objects.requireNonNull(transactionManager);
        this.createPaymentUseCase = Objects.requireNonNull(createPaymentUseCase);
    }

    @Override
    public CreateCheckoutOutput execute(final CreateCheckoutInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createCheckoutUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreateCheckoutUseCase.class);

                    final Map<String, Integer> ticketQuantities = input.items().stream()
                            .collect(Collectors.toMap(CreateCheckoutItemsInput::ticketId, CreateCheckoutItemsInput::quantity));

                    final List<Ticket> aTickets = ctx.runInSpan("ticket.retrieves", () ->
                            ticketQuantities.keySet().stream()
                                    .map(id -> ticketRepository.ticketOfId(id)
                                            .orElseThrow(NotFoundException.with(Ticket.class, id)))
                                    .toList()
                    );

                    final var orderItems = new ArrayList<OrderItem>();
                    final var aUpdatedTickets = new ArrayList<Ticket>();
                    for (Ticket ticket : aTickets) {
                        final int requestedQty = ticketQuantities.get(ticket.getId().value().toString());
                        if (ticket.getQuantity() - ticket.getSold() < requestedQty) {
                            throw DomainException.with("Ticket %s is sold out or does not have enough quantity".formatted(ticket.getId()));
                        }

                        aUpdatedTickets.add(ticket.updateSold(ticket.getSold() + requestedQty));
                        orderItems.add(OrderItem.newItem(ticket.getEventId(), ticket.getId(), requestedQty, ticket.getPrice()));
                    }

                    final var aOrder = Order.newOrder(
                            new UserID(ULID.fromString(input.userId())),
                            orderItems
                    );

                    // TODO Register OrderCreatedEvent
                    aOrder.registerEvent(new OrderCreatedEvent(
                            aOrder.getId().value().toString(),
                            aOrder.getVersion(),
                            aOrder.getStatus().name(),
                            aOrder.getTotalAmount(),
                            createPaymentDetails(input, aOrder.getTotalAmount()),
                            ctx.traceId()
                    ));

                    ctx.setAttribute("orderId", aOrder.getId().value().toString());
                    ctx.setAttribute("order.totalAmount", aOrder.getTotalAmount());
                    ctx.setAttribute("order.itemsCount", aOrder.getItems().size());
                    ctx.setAttribute("userId", aOrder.getUserId().value().toString());
                    ctx.setAttribute("payment.method", input.paymentDetails().method().name());

                    // TODO in future use events to notify other systems
                    final var aTransactionResult = this.transactionManager.execute(() -> {
                        ctx.runInSpan(
                                "ticket.saveAll",
                                () -> this.ticketRepository.saveAll(aUpdatedTickets)
                        );

                        ctx.runInSpan(
                                "order.save",
                                () -> this.orderRepository.save(aOrder)
                        );
                        return true;
                    });

                    if (aTransactionResult.isFailure()) {
                        throw aTransactionResult.getException();
                    }

                    if (input.paymentDetails().method() != PaymentMethod.PIX) {
                        return CreateCheckoutOutput.from(
                                aOrder,
                                input.paymentDetails().method().name(),
                                null,
                                null
                        );
                    }

                    try {
                        final var aPaymentResponse = ctx.runInSpan(
                                "payment.create",
                                () -> this.createPaymentUseCase.execute(CreatePaymentInput.with(
                                        createPaymentDetails(input, aOrder.getTotalAmount()),
                                        aOrder.getId().value().toString(),
                                        ctx.traceId()
                                ))
                        );
                        final var aOrderWithPaymentId = aOrder.updatePaymentId(new PaymentID(ULID.fromString(aPaymentResponse.paymentId())));

                        ctx.runInSpan(
                                "order.save",
                                () -> this.orderRepository.save(aOrderWithPaymentId)
                        );

                        return CreateCheckoutOutput.from(
                                aOrderWithPaymentId,
                                input.paymentDetails().method().name(),
                                aPaymentResponse.qrCode(),
                                aPaymentResponse.qrCodeImageUrl()
                        );
                    } catch (Exception ex) {
                        ctx.runInSpan("order.saveFailed", () -> this.orderRepository.save(aOrder.markAsFailed()));
                        ctx.runInSpan("ticket.rollbackSold", () -> {
                            final var aRollbackTickets = aUpdatedTickets.stream()
                                    .map(t -> t.updateSold(t.getSold() - ticketQuantities.get(t.getId().value().toString())))
                                    .toList();
                            this.ticketRepository.saveAll(aRollbackTickets);
                        });
                        throw DomainException.with("There was an error processing the payment: %s".formatted(ex.getMessage()));
                    }
                }
        );
    }

    private PaymentDetails createPaymentDetails(
            final CreateCheckoutInput aInput,
            final BigDecimal aTotalAmount
    ) {
        return switch (aInput.paymentDetails().method()) {
            case PIX -> new PixPaymentDetails(aTotalAmount);
            case CREDIT_CARD -> new CreditCardPaymentDetails(
                    aTotalAmount,
                    ((CreateCheckoutCreditCardPaymentDetails) aInput.paymentDetails()).name(),
                    ((CreateCheckoutCreditCardPaymentDetails) aInput.paymentDetails()).cpf(),
                    ((CreateCheckoutCreditCardPaymentDetails) aInput.paymentDetails()).email(),
                    ((CreateCheckoutCreditCardPaymentDetails) aInput.paymentDetails()).paymentToken(),
                    ((CreateCheckoutCreditCardPaymentDetails) aInput.paymentDetails()).installments()
            );
            default -> null;
        };
    }
}
