package com.kaua.events.platform.infrastructure.ticket;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class TicketJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(ticketRepository());
    }

    @Test
    void givenAValidNewTicket_whenCallSave_thenTicketIsPersisted() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = ULID.random();
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aTicket = Ticket.newTicket(
                new EventID(aEventId),
                aName,
                aDescription,
                aPrice,
                aQuantity,
                TicketType.from(aType).get(),
                TicketStatus.from(aStatus).get()
        );

        final var aActualTicket = this.ticketRepository().save(aTicket);

        Assertions.assertEquals(1, countTickets());
        Assertions.assertEquals(aTicket.getId(), aActualTicket.getId());
        Assertions.assertEquals(aTicket.getVersion(), aActualTicket.getVersion());
        Assertions.assertEquals(aTicket.getName(), aActualTicket.getName());
        Assertions.assertEquals(aTicket.getDescription().get(), aActualTicket.getDescription().get());
        Assertions.assertEquals(aTicket.getPrice(), aActualTicket.getPrice());
        Assertions.assertEquals(aTicket.getQuantity(), aActualTicket.getQuantity());
        Assertions.assertEquals(aTicket.getType(), aActualTicket.getType());
        Assertions.assertEquals(aTicket.getStatus(), aActualTicket.getStatus());
        Assertions.assertEquals(aTicket.getCreatedAt(), aActualTicket.getCreatedAt());
        Assertions.assertEquals(aTicket.getUpdatedAt(), aActualTicket.getUpdatedAt());
    }
}
