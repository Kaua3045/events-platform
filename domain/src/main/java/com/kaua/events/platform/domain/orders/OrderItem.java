package com.kaua.events.platform.domain.orders;

import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.AssertionConcern;

import java.math.BigDecimal;
import java.math.RoundingMode;

// TODO change BigDecimal to Money value object and setScale default to 2 half up
public class OrderItem implements AssertionConcern {

    private ULID id;
    private EventID eventId;
    private TicketID ticketId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private OrderItem(
            final ULID aId,
            final EventID aEventId,
            final TicketID aTicketId,
            final int aQuantity,
            final BigDecimal aUnitPrice,
            final BigDecimal aTotalPrice
    ) {
        setId(aId);
        setEventId(aEventId);
        setTicketId(aTicketId);
        setQuantity(aQuantity);
        setUnitPrice(aUnitPrice);
        setTotalPrice(aTotalPrice);
    }

    public static OrderItem newItem(
            final EventID aEventId,
            final TicketID aTicketId,
            final int aQuantity,
            final BigDecimal aUnitPrice
    ) {
        return new OrderItem(
                IdentifierUtils.generateNewULID(),
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPrice,
                aUnitPrice.multiply(BigDecimal.valueOf(aQuantity))
        );
    }

    public static OrderItem with(
            final ULID aId,
            final EventID aEventId,
            final TicketID aTicketId,
            final int aQuantity,
            final BigDecimal aUnitPrice,
            final BigDecimal aTotalPrice
    ) {
        return new OrderItem(
                aId,
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPrice,
                aTotalPrice
        );
    }

    public ULID getId() {
        return id;
    }

    public EventID getEventId() {
        return eventId;
    }

    public TicketID getTicketId() {
        return ticketId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    private void setId(final ULID id) {
        this.id = this.assertArgumentNotNull(id, "id", "should not be null");
    }

    private void setEventId(final EventID eventId) {
        this.eventId = this.assertArgumentNotNull(eventId, "eventId", "should not be null");
    }

    private void setTicketId(final TicketID ticketId) {
        this.ticketId = this.assertArgumentNotNull(ticketId, "ticketId", "should not be null");
    }

    private void setQuantity(final int quantity) {
        this.assertArgumentGreaterOrEquals(quantity, 1, "quantity", "cannot be negative or zero");
        this.quantity = quantity;
    }

    private void setUnitPrice(final BigDecimal unitPrice) {
        this.assertArgumentNotNull(unitPrice, "unitPrice", "cannot be null");
        this.assertArgumentTrue(unitPrice.compareTo(BigDecimal.ZERO) > 0, "unitPrice", "cannot be negative");
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private void setTotalPrice(BigDecimal totalPrice) {
        this.assertArgumentNotNull(totalPrice, "totalPrice", "cannot be null");
        this.assertArgumentTrue(totalPrice.compareTo(BigDecimal.ZERO) > 0, "totalPrice", "cannot be negative");
        this.totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "OrderItem(" +
                "id=" + id +
                ", eventId=" + eventId +
                ", ticketId=" + ticketId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ')';
    }
}
