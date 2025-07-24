package com.kaua.events.platform.application.usecases.ticket.retrieve.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class GetTicketByIdUseCaseTest extends UseCaseTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private DefaultGetTicketByIdUseCase useCase;

    @Test
    void givenAValidInput_whenCallsGetTicketById_thenReturnTicket() {
        final var aTicket = Fixture.TicketFixture.newTicket();
        final var aTicketId = aTicket.getId().value().toString();

        final var aInput = GetTicketByIdInput.with(aTicketId);

        Mockito.when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aTicketId, aOutput.ticketId());
        Assertions.assertEquals(aTicket.getEventId().value().toString(), aOutput.eventId());
        Assertions.assertEquals(aTicket.getName(), aOutput.name());
        Assertions.assertEquals(aTicket.getDescription().get(), aOutput.description());
        Assertions.assertEquals(aTicket.getPrice(), aOutput.price());
        Assertions.assertEquals(aTicket.getQuantity(), aOutput.quantity());
        Assertions.assertEquals(aTicket.getSold(), aOutput.sold());
        Assertions.assertEquals(aTicket.getType().name(), aOutput.ticketType());
        Assertions.assertEquals(aTicket.getStatus().name(), aOutput.ticketStatus());
        Assertions.assertEquals(aTicket.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aTicket.getUpdatedAt(), aOutput.updatedAt());

        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(any());
    }

    @Test
    void givenAnInvalidId_whenCallsGetTicketById_thenThrowNotFoundException() {
        final var aTicketId = ULID.random().toString();

        final var expectedErrorMessage = "Ticket with id " + aTicketId + " was not found";

        final var aInput = GetTicketByIdInput.with(aTicketId);

        Mockito.when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.empty());

        final var actualException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, actualException.getMessage());

        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(any());
    }

    @Test
    void givenAInvalidInput_whenCallsGetTicketById_thenThrowUseCaseInputCannotBeNullException() {
        final GetTicketByIdInput aInput = null;

        final var expectedErrorMessage = "Input to GetTicketByIdUseCase cannot be null";

        final var actualException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, actualException.getMessage());

        Mockito.verify(ticketRepository, Mockito.times(0)).ticketOfId(any());
    }
}
