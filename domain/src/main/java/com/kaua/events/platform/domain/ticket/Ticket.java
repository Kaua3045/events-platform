package com.kaua.events.platform.domain.ticket;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class Ticket extends AggregateRoot<TicketID> {

    private String name;
    private String description;
    private EventID eventId;
    private BigDecimal price;
    private int quantity;
    private int sold;
    private TicketType type; // Type of ticket (e.g., General Admission, VIP, etc.)
    private TicketStatus status; // Status of the ticket (e.g., Available, Sold, Cancelled)
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Ticket(
            final TicketID aTicketID,
            final long aVersion,
            final String aName,
            final String aDescription,
            final EventID aEventId,
            final BigDecimal aPrice,
            final int aQuantity,
            final int aSold,
            final TicketType aType,
            final TicketStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        super(aTicketID, aVersion);
        setName(aName);
        setDescription(aDescription);
        setEventId(aEventId);
        setPrice(aPrice);
        setQuantity(aQuantity);
        setSold(aSold);
        setType(aType);
        setStatus(aStatus);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setDeletedAt(aDeletedAt);
    }

    public static Ticket newTicket(
            final EventID aEventId,
            final String aName,
            final String aDescription,
            final BigDecimal aPrice,
            final int aQuantity,
            final TicketType aType,
            final TicketStatus aStatus
    ) {
        final var aId = new TicketID(IdentifierUtils.generateNewMonotonicULID());
        final var aNow = InstantUtils.now();

        return new Ticket(
                aId,
                0L,
                aName,
                aDescription,
                aEventId,
                aPrice,
                aQuantity,
                0, // Initially sold is 0
                aType,
                aStatus,
                aNow,
                aNow,
                null
        );
    }

    public static Ticket with(
            final TicketID aTicketID,
            final long aVersion,
            final String aName,
            final String aDescription,
            final EventID aEventId,
            final BigDecimal aPrice,
            final int aQuantity,
            final int aSold,
            final TicketType aType,
            final TicketStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        return new Ticket(
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
                aCreatedAt,
                aUpdatedAt,
                aDeletedAt
        );
    }

    public Ticket update(
            final String aName,
            final String aDescription,
            final BigDecimal aPrice,
            final int aQuantity,
            final TicketType aType,
            final TicketStatus aStatus
    ) {
        // TODO validate if not accepting deleted status
        return new Ticket(
                getId(),
                getVersion(),
                aName,
                aDescription,
                eventId,
                aPrice,
                aQuantity,
                sold,
                aType,
                aStatus,
                createdAt,
                InstantUtils.now(),
                null
        );
    }

    public Ticket markAsDeleted() {
        return new Ticket(
                getId(),
                getVersion(),
                name,
                description,
                eventId,
                price,
                quantity,
                sold,
                type,
                TicketStatus.DELETED,
                createdAt,
                InstantUtils.now(),
                InstantUtils.now()
        );
    }
    
    public Ticket updateSold(final int aSold) {
        return new Ticket(
                getId(),
                getVersion(),
                getName(),
                getDescription().orElse(null),
                getEventId(),
                getPrice(),
                getQuantity(),
                aSold,
                getType(),
                getStatus(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt().orElse(null)
        );
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public EventID getEventId() {
        return eventId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSold() {
        return sold;
    }

    public TicketType getType() {
        return type;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    private void setName(final String name) {
        this.assertArgumentNotNull(name, "name", "cannot be null");
        this.assertArgumentMaxLength(name, 100, "name", "cannot be longer than 100 characters");
        this.assertArgumentMinLength(name, 5, "name", "cannot be shorter than 5 characters");
        this.name = name;
    }

    private void setDescription(final String description) {
        this.assertArgumentMaxLength(description, 255, "description", "cannot be longer than 255 characters");
        this.description = description;
    }

    private void setEventId(final EventID eventId) {
        this.assertArgumentNotNull(eventId, "eventId", "cannot be null");
        this.eventId = eventId;
    }

    private void setPrice(final BigDecimal price) {
        this.assertArgumentNotNull(price, "price", "cannot be null");
        this.assertArgumentTrue(price.compareTo(BigDecimal.ZERO) > 0, "price", "cannot be negative");
        this.price = price;
    }

    private void setQuantity(final int quantity) {
        this.assertArgumentGreaterOrEquals(quantity, 0, "quantity", "cannot be negative");
        this.quantity = quantity;
    }

    private void setSold(final int sold) {
        this.assertArgumentGreaterOrEquals(sold, 0, "sold", "cannot be negative");
        this.assertArgumentTrue(sold <= this.quantity, "sold", "cannot be greater than quantity");
        this.sold = sold;
    }

    private void setType(final TicketType type) {
        this.assertArgumentNotNull(type, "type", "cannot be null");
        this.type = type;
    }

    private void setStatus(final TicketStatus status) {
        this.assertArgumentNotNull(status, "status", "cannot be null");
        this.status = status;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "cannot be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "cannot be null");
    }

    private void setDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "Ticket(" +
                "id='" + getId().value().toString() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + getDescription().orElse(null) + '\'' +
                ", eventId=" + eventId.value().toString() +
                ", price=" + price +
                ", quantity=" + quantity +
                ", sold=" + sold +
                ", type=" + type.name() +
                ", status=" + status.name() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + getDeletedAt().map(Instant::toString).orElse(null) +
                ')';
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Generated
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Ticket ticket)) return false;
        if (!super.equals(object)) return false;
        return quantity == ticket.quantity && sold == ticket.sold && Objects.equals(name, ticket.name) && Objects.equals(description, ticket.description) && Objects.equals(eventId, ticket.eventId) && Objects.equals(price, ticket.price) && type == ticket.type && status == ticket.status;
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, eventId, price, quantity, sold, type, status);
    }
}
