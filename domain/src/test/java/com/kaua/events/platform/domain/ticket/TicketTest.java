package com.kaua.events.platform.domain.ticket;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class TicketTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewTicket_thenInstantiateAnTicket() {
        final var aEventId = new EventID(ULID.random());
        final var aName = "General Admission";
        final var aDescription = "Access to the event";
        final var aPrice = BigDecimal.valueOf(50.00);
        final var aQuantity = 100;
        final var aType = TicketType.STANDARD;
        final var aStatus = TicketStatus.AVAILABLE;

        final var aTicket = Ticket.newTicket(
                aEventId,
                aName,
                aDescription,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Assertions.assertNotNull(aTicket);
        Assertions.assertNotNull(aTicket.getId());
        Assertions.assertEquals(aName, aTicket.getName());
        Assertions.assertEquals(aDescription, aTicket.getDescription().get());
        Assertions.assertEquals(aEventId, aTicket.getEventId());
        Assertions.assertEquals(aPrice, aTicket.getPrice());
        Assertions.assertEquals(aQuantity, aTicket.getQuantity());
        Assertions.assertEquals(0, aTicket.getSold());
        Assertions.assertEquals(aType, aTicket.getType());
        Assertions.assertEquals(aStatus, aTicket.getStatus());
        Assertions.assertNotNull(aTicket.getCreatedAt());
        Assertions.assertNotNull(aTicket.getUpdatedAt());
        Assertions.assertDoesNotThrow(() -> aTicket.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWith_thenInstantiateAnTicket() {
        final var aTicketID = new TicketID(ULID.random());
        final var aVersion = 1;
        final var aEventId = new EventID(ULID.random());
        final var aName = "VIP Access";
        final var aDescription = "Access to VIP area";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 50;
        final var aSold = 0;
        final var aType = TicketType.VIP;
        final var aStatus = TicketStatus.AVAILABLE;
        final var aNow = InstantUtils.now();

        final var aTicket = Ticket.with(
                aTicketID,
                aVersion,
                aName,
                aDescription,
                aEventId,
                aPrice,
                aQuantity,
                aSold,
                aType,
                aStatus,
                aNow,
                aNow,
                null
        );

        Assertions.assertNotNull(aTicket);
        Assertions.assertEquals(aTicketID, aTicket.getId());
        Assertions.assertEquals(aVersion, aTicket.getVersion());
        Assertions.assertEquals(aName, aTicket.getName());
        Assertions.assertEquals(aDescription, aTicket.getDescription().get());
        Assertions.assertEquals(aEventId, aTicket.getEventId());
        Assertions.assertEquals(aPrice, aTicket.getPrice());
        Assertions.assertEquals(aQuantity, aTicket.getQuantity());
        Assertions.assertEquals(aSold, aTicket.getSold());
        Assertions.assertEquals(aType, aTicket.getType());
        Assertions.assertEquals(aStatus, aTicket.getStatus());
        Assertions.assertEquals(aNow, aTicket.getCreatedAt());
        Assertions.assertEquals(aNow, aTicket.getUpdatedAt());
    }

    @Test
    void testCallToStringInTicket() {
        final var aTicketID = new TicketID(ULID.random());
        final var aVersion = 1;
        final var aEventId = new EventID(ULID.random());
        final var aName = "General Admission";
        final var aDescription = "Access to the event";
        final var aPrice = BigDecimal.valueOf(50.00);
        final var aQuantity = 100;
        final var aSold = 0;
        final var aType = TicketType.STANDARD;
        final var aStatus = TicketStatus.AVAILABLE;
        final var aNow = InstantUtils.now();

        final var aTicket = Ticket.with(
                aTicketID,
                aVersion,
                aName,
                aDescription,
                aEventId,
                aPrice,
                aQuantity,
                aSold,
                aType,
                aStatus,
                aNow,
                aNow,
                null
        );

        Assertions.assertNotNull(aTicket.toString());
    }

    @Test
    void givenAValidValue_whenCallTicketTypeFrom_thenReturnTicketType() {
        final var aTicketType = TicketType.from("VIP");

        Assertions.assertNotNull(aTicketType);
        Assertions.assertEquals(TicketType.VIP, aTicketType.get());
    }

    @Test
    void givenAnInvalidValue_whenCallTicketTypeFrom_thenReturnEmpty() {
        final var aTicketType = TicketType.from("INVALID");

        Assertions.assertTrue(aTicketType.isEmpty());
    }

    @Test
    void givenAValidValue_whenCallTicketStatusFrom_thenReturnTicketStatus() {
        final var aTicketStatus = TicketStatus.from("AVAILABLE");

        Assertions.assertNotNull(aTicketStatus);
        Assertions.assertEquals(TicketStatus.AVAILABLE, aTicketStatus.get());
    }

    @Test
    void givenAnInvalidValue_whenCallTicketStatusFrom_thenReturnEmpty() {
        final var aTicketStatus = TicketStatus.from("INVALID");

        Assertions.assertTrue(aTicketStatus.isEmpty());
    }

    @Test
    void givenAnInvalidNegativePrice_whenCallNewTicket_thenThrowValidationException() {
        final var aEventId = new EventID(ULID.random());
        final var aName = "General Admission";
        final var aDescription = "Access to the event";
        final var aPrice = BigDecimal.valueOf(-50.00);
        final var aQuantity = 100;
        final var aType = TicketType.STANDARD;
        final var aStatus = TicketStatus.AVAILABLE;

        final var expectedProperty = "price";
        final var expectedErrorMessage = "cannot be negative";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> Ticket.newTicket(
                        aEventId,
                        aName,
                        aDescription,
                        aPrice,
                        aQuantity,
                        aType,
                        aStatus
                ));

        Assertions.assertEquals(expectedProperty, aException.getErrors().getFirst().property());
        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
    }

    @Test
    void givenAnInvalidInvalidSold_whenCallNewTicket_thenThrowValidationException() {
        final var aTicketID = new TicketID(ULID.random());
        final var aVersion = 1;
        final var aEventId = new EventID(ULID.random());
        final var aName = "VIP Access";
        final var aDescription = "Access to VIP area";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 50;
        final var aSold = 100;
        final var aType = TicketType.VIP;
        final var aStatus = TicketStatus.AVAILABLE;
        final var aNow = InstantUtils.now();

        final var expectedProperty = "sold";
        final var expectedErrorMessage = "cannot be greater than quantity";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> Ticket.with(
                        aTicketID,
                        aVersion,
                        aName,
                        aDescription,
                        aEventId,
                        aPrice,
                        aQuantity,
                        aSold,
                        aType,
                        aStatus,
                        aNow,
                        aNow,
                        null
                ));

        Assertions.assertEquals(expectedProperty, aException.getErrors().getFirst().property());
        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
    }

    @Test
    void givenAValidValues_whenCallUpdate_thenReturnUpdatedTicket() {
        final var aTicketID = new TicketID(ULID.random());
        final var aVersion = 1;
        final var aEventId = new EventID(ULID.random());
        final var aName = "VIP Access";
        final var aDescription = "Access to VIP area";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 50;
        final var aSold = 0;
        final var aType = TicketType.VIP;
        final var aStatus = TicketStatus.AVAILABLE;
        final var aNow = InstantUtils.now();

        final var aTicket = Ticket.with(
                aTicketID,
                aVersion,
                aName,
                aDescription,
                aEventId,
                aPrice,
                aQuantity,
                aSold,
                aType,
                aStatus,
                aNow,
                aNow,
                null
        );

        final var aUpdatedName = "Updated VIP Access";
        final var aUpdatedDescription = "Updated access to VIP area";
        final var aUpdatedPrice = BigDecimal.valueOf(120.00);
        final var aUpdatedQuantity = 60;

        final var aUpdatedTicket = aTicket.update(
                aUpdatedName,
                aUpdatedDescription,
                aUpdatedPrice,
                aUpdatedQuantity,
                aTicket.getType(),
                aTicket.getStatus()
        );

        Assertions.assertNotNull(aUpdatedTicket);
        Assertions.assertEquals(aTicketID, aUpdatedTicket.getId());
        Assertions.assertEquals(aUpdatedName, aUpdatedTicket.getName());
        Assertions.assertEquals(aUpdatedDescription, aUpdatedTicket.getDescription().get());
        Assertions.assertEquals(aEventId, aUpdatedTicket.getEventId());
        Assertions.assertEquals(aUpdatedPrice, aUpdatedTicket.getPrice());
        Assertions.assertEquals(aUpdatedQuantity, aUpdatedTicket.getQuantity());
    }

    @Test
    void givenAValidTicket_whenCallMarkAsDeleted_thenReturnDeletedTicket() {
        final var aTicketID = new TicketID(ULID.random());
        final var aVersion = 1;
        final var aEventId = new EventID(ULID.random());
        final var aName = "VIP Access";
        final var aDescription = "Access to VIP area";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 50;
        final var aSold = 0;
        final var aType = TicketType.VIP;
        final var aStatus = TicketStatus.AVAILABLE;
        final var aNow = InstantUtils.now();

        final var aTicket = Ticket.with(
                aTicketID,
                aVersion,
                aName,
                aDescription,
                aEventId,
                aPrice,
                aQuantity,
                aSold,
                aType,
                aStatus,
                aNow,
                aNow,
                null
        );

        final var aDeletedTicket = aTicket.markAsDeleted();

        Assertions.assertNotNull(aDeletedTicket);
        Assertions.assertEquals(aTicketID, aDeletedTicket.getId());
        Assertions.assertEquals(TicketStatus.DELETED, aDeletedTicket.getStatus());
        Assertions.assertTrue(aDeletedTicket.getDeletedAt().isPresent());
    }
}
