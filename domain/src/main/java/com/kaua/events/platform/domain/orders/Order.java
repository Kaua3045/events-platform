package com.kaua.events.platform.domain.orders;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.orders.events.OrderStatusChangedEvent;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// TODO change BigDecimal to Money value object and setScale default to 2 half up
public class Order extends AggregateRoot<OrderID> {

    private UserID userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private PaymentID paymentId;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant failedAt;

    private Order(
            final OrderID aOrderID,
            final long aVersion,
            final UserID aUserId,
            final List<OrderItem> aItems,
            final BigDecimal aTotalAmount,
            final PaymentID aPaymentId,
            final OrderStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aFailedAt
    ) {
        super(aOrderID, aVersion);
        setUserId(aUserId);
        setItems(aItems);
        setTotalAmount(aTotalAmount);
        setPaymentId(aPaymentId);
        setStatus(aStatus);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setFailedAt(aFailedAt);
    }

    public static Order newOrder(
            final UserID aUserId,
            final List<OrderItem> aItems
    ) {
        final var aNow = InstantUtils.now();

        return new Order(
                new OrderID(IdentifierUtils.generateNewULID()),
                0L,
                aUserId,
                aItems,
                aItems.stream().map(OrderItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                null,
                OrderStatus.CREATED,
                aNow,
                aNow,
                null
        );
    }

    public static Order with(
            final OrderID aId,
            final long aVersion,
            final UserID aUserId,
            final List<OrderItem> aItems,
            final BigDecimal aTotalAmount,
            final PaymentID aPaymentId,
            final OrderStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aFailedAt
    ) {
        return new Order(
                aId,
                aVersion,
                aUserId,
                aItems,
                aTotalAmount,
                aPaymentId,
                aStatus,
                aCreatedAt,
                aUpdatedAt,
                aFailedAt
        );
    }

    public Order updatePaymentId(final PaymentID aPaymentId) {
        return new Order(
                getId(),
                getVersion(),
                getUserId(),
                getItems(),
                getTotalAmount(),
                aPaymentId,
                getStatus(),
                getCreatedAt(),
                InstantUtils.now(),
                getFailedAt().orElse(null)
        );
    }

    public Order markAsFailed() {
        final var aOrder = new Order(
                getId(),
                getVersion(),
                getUserId(),
                getItems(),
                getTotalAmount(),
                getPaymentId().orElse(null),
                OrderStatus.FAILED,
                getCreatedAt(),
                InstantUtils.now(),
                InstantUtils.now()
        );
        aOrder.registerEvent(new OrderStatusChangedEvent(
                aOrder.getId().value().toString(),
                aOrder.getVersion(),
                aOrder.getStatus().name()
        ));

        return aOrder;
    }

    public Order markAsPaymentPending(PaymentID paymentId) {
        final var aOrder = new Order(
                getId(),
                getVersion(),
                getUserId(),
                getItems(),
                getTotalAmount(),
                paymentId,
                OrderStatus.PAYMENT_PENDING,
                getCreatedAt(),
                InstantUtils.now(),
                null
        );

        aOrder.registerEvent(new OrderStatusChangedEvent(
                aOrder.getId().value().toString(),
                aOrder.getVersion(),
                aOrder.getStatus().name()
        ));

        return aOrder;
    }

    public Order markAsPaymentApproved() {
        final var aOrder = new Order(
                getId(),
                getVersion(),
                getUserId(),
                getItems(),
                getTotalAmount(),
                getPaymentId().orElse(null),
                OrderStatus.PAYMENT_APPROVED,
                getCreatedAt(),
                InstantUtils.now(),
                null
        );

        aOrder.registerEvent(new OrderStatusChangedEvent(
                aOrder.getId().value().toString(),
                aOrder.getVersion(),
                aOrder.getStatus().name()
        ));

        return aOrder;
    }

    public Order markAsPaid() {
        final var aOrder = new Order(
                getId(),
                getVersion(),
                getUserId(),
                getItems(),
                getTotalAmount(),
                getPaymentId().orElse(null),
                OrderStatus.PAID,
                getCreatedAt(),
                InstantUtils.now(),
                null
        );

        aOrder.registerEvent(new OrderStatusChangedEvent(
                aOrder.getId().value().toString(),
                aOrder.getVersion(),
                aOrder.getStatus().name()
        ));

        return aOrder;
    }

    @Generated
    // TODO use this to ignore in coverage, but in future remove new order and with receive items, and use this method or add
    // if to check total is 0 or null, on is 0 or null throws exception
    public void addAllItem(final List<OrderItem> aItem) {
        this.items.addAll(aItem);
    }

    public UserID getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Optional<PaymentID> getPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getFailedAt() {
        return Optional.ofNullable(failedAt);
    }

    private void setUserId(final UserID userId) {
        this.assertArgumentNotNull(userId, "userId", "should not be null");
        this.userId = userId;
    }

    private void setItems(final List<OrderItem> items) {
        this.items = items;
    }

    private void setTotalAmount(final BigDecimal totalAmount) {
        this.assertArgumentNotNull(totalAmount, "totalAmount", "cannot be null");
        this.assertArgumentTrue(totalAmount.compareTo(BigDecimal.ZERO) > 0, "totalAmount", "cannot be negative");
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private void setPaymentId(final PaymentID paymentId) {
        this.paymentId = paymentId;
    }

    private void setStatus(final OrderStatus status) {
        this.assertArgumentNotNull(status, "status", "should not be null");
        this.status = status;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
        this.createdAt = createdAt;
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
        this.updatedAt = updatedAt;
    }

    private void setFailedAt(final Instant failedAt) {
        this.failedAt = failedAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "Order(" +
                "id=" + getId().value().toString() +
                ", version=" + getVersion() +
                ", userId=" + getUserId().value().toString() +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", paymentId=" + getPaymentId().orElse(null) +
                ", status=" + getStatus().name() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                ", failedAt=" + getFailedAt().orElse(null) +
                ')';
    }
}
