package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class Payment extends AggregateRoot<PaymentID> {

    private OrderID orderId;
    private String transactionId;
    private PaymentStatus status;
    private PaymentMethod method;
    private BigDecimal amount;

    private PaymentDetails paymentDetails;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;

    private Payment(
            final PaymentID aPaymentID,
            final long aVersion,
            final OrderID aOrderId,
            final String aTransactionId,
            final PaymentStatus aStatus,
            final PaymentMethod aMethod,
            final BigDecimal aAmount,
            final PaymentDetails aPaymentDetails,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aPaidAt
    ) {
        super(aPaymentID, aVersion);
        setOrderId(aOrderId);
        setTransactionId(aTransactionId);
        setStatus(aStatus);
        setMethod(aMethod);
        setAmount(aAmount);
        setPaymentDetails(aPaymentDetails);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setPaidAt(aPaidAt);
    }

    public static Payment newPayment(
            final OrderID aOrderId,
            final PaymentMethod aMethod,
            final PaymentDetails aPaymentDetails,
            final BigDecimal aAmount
    ) {
        final var aNow = InstantUtils.now();

        return new Payment(
                new PaymentID(IdentifierUtils.generateNewULID()),
                0L,
                aOrderId,
                IdentifierUtils.generateNewIdWithoutHyphen(),
                PaymentStatus.NEW,
                aMethod,
                aAmount,
                aPaymentDetails,
                aNow,
                aNow,
                null
        );
    }

    public static Payment with(
            final PaymentID aPaymentId,
            final long aVersion,
            final OrderID aOrderId,
            final String aTransactionId,
            final PaymentStatus aStatus,
            final PaymentMethod aMethod,
            final BigDecimal aAmount,
            final PaymentDetails aPaymentDetails,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aPaidAt
    ) {
        return new Payment(
                aPaymentId,
                aVersion,
                aOrderId,
                aTransactionId,
                aStatus,
                aMethod,
                aAmount,
                aPaymentDetails,
                aCreatedAt,
                aUpdatedAt,
                aPaidAt
        );
    }

    public Payment markAsPending(final PaymentDetails aPaymentDetails) {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.PENDING,
                getMethod(),
                getAmount(),
                aPaymentDetails,
                getCreatedAt(),
                InstantUtils.now(),
                null
        );
    }

    public Payment markAsPaid() {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.PAID,
                getMethod(),
                getAmount(),
                getPaymentDetails(),
                getCreatedAt(),
                InstantUtils.now(),
                InstantUtils.now()
        );
    }

    public Payment markAsApproved() {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.APPROVED,
                getMethod(),
                getAmount(),
                getPaymentDetails(),
                getCreatedAt(),
                InstantUtils.now(),
                getPaidAt().orElse(null)
        );
    }

    public Payment markAsIdentified() {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.IDENTIFIED,
                getMethod(),
                getAmount(),
                getPaymentDetails(),
                getCreatedAt(),
                InstantUtils.now(),
                getPaidAt().orElse(null)
        );
    }

    public Payment markAsFailed() {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.FAILED,
                getMethod(),
                getAmount(),
                getPaymentDetails(),
                getCreatedAt(),
                InstantUtils.now(),
                getPaidAt().orElse(null)
        );
    }

    public OrderID getOrderId() {
        return orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getPaidAt() {
        return Optional.ofNullable(paidAt);
    }

    private void setOrderId(final OrderID orderId) {
        this.orderId = this.assertArgumentNotNull(orderId, "orderId", "should not be null");
    }

    private void setTransactionId(final String transactionId) {
        this.transactionId = this.assertArgumentNotEmpty(transactionId, "transactionId", "should not be empty");
    }

    private void setStatus(final PaymentStatus status) {
        this.status = this.assertArgumentNotNull(status, "status", "should not be null");
    }

    private void setMethod(final PaymentMethod method) {
        this.method = this.assertArgumentNotNull(method, "method", "should not be null");
    }

    private void setAmount(final BigDecimal amount) {
        this.assertArgumentNotNull(amount, "amount", "should not be null");
        this.amount = amount;
    }

    public void setPaymentDetails(final PaymentDetails paymentDetails) {
        this.paymentDetails = this.assertArgumentNotNull(paymentDetails, "paymentDetails", "should not be null");
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
    }

    private void setPaidAt(final Instant paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "Payment(" +
                "id='" + getId().value().toString() + '\'' +
                ", version=" + getVersion() +
                ", transactionId=" + transactionId +
                ", status=" + status.name() +
                ", method=" + method.name() +
                ", amount=" + amount +
                ", paymentDetails='" + getPaymentDetails() + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", paidAt=" + getPaidAt().orElse(null) +
                ')';
    }
}
