package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutCreditCardPaymentDetails;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPaymentDetailsInput;
import com.kaua.events.platform.application.usecases.orders.create.payment.CreateCheckoutPixPaymentDetails;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentOutput;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentUseCase;
import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.application.wrapper.TransactionResult;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreateCheckoutUseCaseTest extends UseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private CreatePaymentUseCase createPaymentUseCase;

    @InjectMocks
    private DefaultCreateCheckoutUseCase useCase;

    @BeforeEach
    void setup() {
        Mockito.when(transactionManager.execute(Mockito.any()))
                .thenAnswer(invocation -> {
                    final var supplier = invocation.getArgument(0, Supplier.class);
                    try {
                        Object result = supplier.get();
                        return TransactionResult.success(result);
                    } catch (RuntimeException e) {
                        return TransactionResult.failure(e);
                    }
                });
    }

    @Test
    void givenValidInputWithPixPayment_whenExecute_thenCreateOrderAndReturnQrCode() {
        final var userId = randomId();
        final var eventId = randomId();
        final var document = "448.370.900-36";
        final var ticket = newTicket("VIP Ticket", BigDecimal.valueOf(100), 2, TicketType.PROMOTIONAL);
        final var input = createCheckoutInput(document, eventId.toString(), userId.toString(), ticket, 2, new CreateCheckoutPixPaymentDetails());

        mockTicketFound(ticket);
        mockOrderSave();
        Mockito.when(createPaymentUseCase.execute(Mockito.any()))
                .thenReturn(new CreatePaymentOutput(
                        ULID.random().toString(),
                        "qrCode",
                        "qrCodeImage")
                );

        final var output = Assertions.assertDoesNotThrow(() -> useCase.execute(input));

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(output.getOrderId());
        Assertions.assertTrue(output.getQrCodeUrl().isPresent());
        Assertions.assertTrue(output.getQrCodeImageUrl().isPresent());
        Assertions.assertEquals(PaymentMethod.PIX.name(), output.getPaymentMethod());
        Assertions.assertNotNull(output.getOrderId());

        Mockito.verify(tracerWrapper).traceWithReturn(Mockito.eq("createCheckoutUseCase"), Mockito.any());
        Mockito.verify(ticketRepository, Mockito.times(1)).saveAll(Mockito.anyList());
        Mockito.verify(orderRepository, Mockito.times(2)).save(argThat(o -> o.getItems().size() == 1));
    }

    @Test
    void givenValidInputWithCreditCardPayment_whenExecute_thenCreateOrderWithoutQrCode() {
        final var userId = randomId();
        final var eventId = randomId();
        final var document = "448.370.900-36";
        final var ticket = newTicket("Regular Ticket", BigDecimal.valueOf(50), 1, TicketType.STANDARD);
        final var paymentDetails = new CreateCheckoutCreditCardPaymentDetails(
                "John Doe",
                "448.370.900-36",
                "john.doe@mail.com",
                "valid-token",
                1
        );
        final var input = createCheckoutInput(document, eventId.toString(), userId.toString(), ticket, 1, paymentDetails);

        mockTicketFound(ticket);
        mockOrderSave();

        final var output = Assertions.assertDoesNotThrow(() -> useCase.execute(input));

        Assertions.assertNotNull(output);
        Assertions.assertEquals(PaymentMethod.CREDIT_CARD.name(), output.getPaymentMethod());
        Assertions.assertTrue(output.getQrCodeUrl().isEmpty());

        Mockito.verify(ticketRepository).saveAll(Mockito.anyList());
        Mockito.verify(orderRepository).save(argThat(o -> o.getItems().size() == 1));
    }

    @Test
    void givenNonExistingTicket_whenExecute_thenThrowNotFoundException() {
        final var userId = randomId();
        final var eventId = randomId();
        final var fakeTicketId = randomId();
        final var document = "448.370.900-36";
        final var paymentDetails = new CreateCheckoutPixPaymentDetails();
        final var input = CreateCheckoutInput.with(
                userId.toString(),
                document,
                "cpf",
                List.of(CreateCheckoutItemsInput.with(eventId.toString(), fakeTicketId.toString(), 1)),
                paymentDetails
        );

        mockTicketNotFound(fakeTicketId.toString());

        Assertions.assertThrows(NotFoundException.class, () -> useCase.execute(input));

        Mockito.verify(ticketRepository, Mockito.never()).saveAll(Mockito.anyList());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenQuantityGreaterThanAvailable_whenExecute_thenThrowDomainException() {
        final var userId = randomId();
        final var eventId = randomId();
        final var ticket = newTicket("Limited Ticket", BigDecimal.valueOf(20), 4, TicketType.VIP);
        final var document = "448.370.900-36";
        final var input = createCheckoutInput(document, eventId.toString(), userId.toString(), ticket, 5, new CreateCheckoutPixPaymentDetails());

        mockTicketFound(ticket);

        Assertions.assertThrows(DomainException.class, () -> useCase.execute(input));

        Mockito.verify(ticketRepository, Mockito.never()).saveAll(Mockito.anyList());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenNullInput_whenExecute_thenThrowUseCaseInputCannotBeNullException() {
        final var ex = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> useCase.execute(null)
        );

        Assertions.assertTrue(ex.getMessage().contains("CreateCheckoutUseCase"));
        Mockito.verify(ticketRepository, Mockito.never()).saveAll(Mockito.anyList());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenTransactionFailure_whenExecute_thenThrowDomainException() {
        final var userId = randomId();
        final var eventId = randomId();
        final var ticket = newTicket("VIP standard", BigDecimal.valueOf(100), 2, TicketType.STANDARD);
        final var input = createCheckoutInput("448.370.900-36", eventId.toString(), userId.toString(), ticket, 1, new CreateCheckoutPixPaymentDetails());

        mockTicketFound(ticket);

        Mockito.when(transactionManager.execute(Mockito.any()))
                .thenReturn(TransactionResult.failure(new RuntimeException("DB error")));

        Assertions.assertThrows(RuntimeException.class, () -> useCase.execute(input));

        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenPixPaymentFails_whenExecute_thenThrowDomainException() {
        final var userId = randomId();
        final var eventId = randomId();
        final var ticket = newTicket("VIP pista", BigDecimal.valueOf(100), 2, TicketType.STANDARD);
        final var input = createCheckoutInput("448.370.900-36", eventId.toString(), userId.toString(), ticket, 1, new CreateCheckoutPixPaymentDetails());

        mockTicketFound(ticket);
        mockOrderSave();

        Mockito.when(createPaymentUseCase.execute(Mockito.any()))
                .thenThrow(new RuntimeException("Payment service down"));
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
                .thenAnswer(returnsFirstArg());
        Mockito.when(ticketRepository.saveAll(Mockito.anyList()))
                .thenAnswer(returnsFirstArg());

        final var ex = Assertions.assertThrows(DomainException.class, () -> useCase.execute(input));
        Assertions.assertTrue(ex.getMessage().contains("There was an error processing the payment"));

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any(Order.class));
        Mockito.verify(ticketRepository, Mockito.times(2)).saveAll(Mockito.anyList());
    }

    private ULID randomId() {
        return ULID.random();
    }

    private Ticket newTicket(String name, BigDecimal price, int availableQuantity, TicketType type) {
        final var eventId = randomId();
        return Ticket.newTicket(
                new EventID(eventId),
                name,
                "description",
                price,
                availableQuantity,
                type,
                TicketStatus.AVAILABLE
        );
    }

    private CreateCheckoutInput createCheckoutInput(String documentNumber, String eventId, String userId, Ticket ticket, int quantity, CreateCheckoutPaymentDetailsInput payment) {
        final var items = List.of(CreateCheckoutItemsInput
                .with(eventId, ticket.getId().value().toString(), quantity));
        return CreateCheckoutInput.with(userId, documentNumber, "cpf", items, payment);
    }

    private void mockTicketFound(Ticket ticket) {
        Mockito.when(ticketRepository.ticketOfId(ticket.getId().value().toString()))
                .thenReturn(Optional.of(ticket));
    }

    private void mockTicketNotFound(String ticketId) {
        Mockito.when(ticketRepository.ticketOfId(ticketId)).thenReturn(Optional.empty());
    }

    private void mockOrderSave() {
        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenAnswer(returnsFirstArg());
    }
}
