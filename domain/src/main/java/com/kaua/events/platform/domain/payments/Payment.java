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

    private String qrCode;
    private String qrCodeImageUrl;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;
    private int expiresIn;

    private Payment(
            final PaymentID aPaymentID,
            final long aVersion,
            final OrderID aOrderId,
            final String aTransactionId,
            final PaymentStatus aStatus,
            final PaymentMethod aMethod,
            final BigDecimal aAmount,
            final String aQrCode,
            final String aQrCodeImageUrl,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aPaidAt,
            final int aExpiresIn
    ) {
        super(aPaymentID, aVersion);
        setOrderId(aOrderId);
        setTransactionId(aTransactionId);
        setStatus(aStatus);
        setMethod(aMethod);
        setAmount(aAmount);
        setQrCode(aQrCode);
        setQrCodeImageUrl(aQrCodeImageUrl);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setPaidAt(aPaidAt);
        setExpiresIn(aExpiresIn);
    }

    public static Payment newPayment(
            final OrderID aOrderId,
            final PaymentMethod aMethod,
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
                null,
                null,
                aNow,
                aNow,
                null,
                0
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
            final String aQrCode,
            final String aQrCodeImageUrl,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aPaidAt,
            final int aExpiresIn
    ) {
        return new Payment(
                aPaymentId,
                aVersion,
                aOrderId,
                aTransactionId,
                aStatus,
                aMethod,
                aAmount,
                aQrCode,
                aQrCodeImageUrl,
                aCreatedAt,
                aUpdatedAt,
                aPaidAt,
                aExpiresIn
        );
    }

    public Payment markAsPending(final int aExpiresIn, final String aQrCode, final String aQrCodeImageUrl) {
        return new Payment(
                getId(),
                getVersion(),
                getOrderId(),
                getTransactionId(),
                PaymentStatus.PENDING,
                getMethod(),
                getAmount(),
                aQrCode,
                aQrCodeImageUrl,
                getCreatedAt(),
                InstantUtils.now(),
                null,
                aExpiresIn
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
                getQrCode().orElse(null),
                getQrCodeImageUrl().orElse(null),
                getCreatedAt(),
                InstantUtils.now(),
                InstantUtils.now(),
                getExpiresIn()
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

    public Optional<String> getQrCode() {
        return Optional.ofNullable(qrCode);
    }

    public Optional<String> getQrCodeImageUrl() {
        return Optional.ofNullable(qrCodeImageUrl);
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

    public int getExpiresIn() {
        return expiresIn;
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

    private void setQrCode(final String qrCode) {
        this.qrCode = qrCode;
    }

    private void setQrCodeImageUrl(final String qrCodeImageUrl) {
        this.qrCodeImageUrl = qrCodeImageUrl;
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

    private void setExpiresIn(final int expiresIn) {
        this.expiresIn = expiresIn;
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
                ", qrCode='" + getQrCode().orElse(null) + '\'' +
                ", qrCodeImageUrl='" + getQrCodeImageUrl().orElse(null) + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", paidAt=" + getPaidAt().orElse(null) +
                ", expiresIn=" + expiresIn +
                ')';
    }
}
