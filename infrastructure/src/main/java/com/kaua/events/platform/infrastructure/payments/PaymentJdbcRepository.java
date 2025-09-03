package com.kaua.events.platform.infrastructure.payments;

import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.payments.*;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import com.kaua.events.platform.infrastructure.jdbc.JdbcUtils;
import com.kaua.events.platform.infrastructure.jdbc.RowMap;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class PaymentJdbcRepository implements PaymentRepository {

    private static final Logger log = LoggerFactory.getLogger(PaymentJdbcRepository.class);

    private final DatabaseClient databaseClient;
    private final OutboxJdbcRepository outboxRepository;

    public PaymentJdbcRepository(
            final DatabaseClient databaseClient,
            final OutboxJdbcRepository outboxRepository
    ) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Payment> paymentOfOrderId(final String orderId) {
        final var aSql = "SELECT * FROM payments p INNER JOIN payment_details d ON p.id = d.id WHERE p.order_id = :orderId";
        return this.databaseClient.queryOne(aSql, Map.of("orderId", orderId), paymentMapper());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Payment save(final Payment payment) {
        if (payment.getVersion() == 0) {
            log.debug("Creating new payment: {}", payment);
            create(payment);
            this.outboxRepository.save(payment.getDomainEvents());
            log.info("Created new payment: {}", payment);
        } else {
            log.debug("Updating payment: {}", payment);
            update(payment);
            this.outboxRepository.save(payment.getDomainEvents());
            log.info("Updated payment: {}", payment);
        }

        payment.incrementVersion();
        return payment;
    }

    private void create(final Payment aPayment) {
        final var aSql = """
                INSERT INTO payments (
                id,
                version,
                order_id,
                transaction_id,
                status,
                method,
                amount,
                created_at,
                updated_at,
                paid_at
                )
                VALUES (
                :id,
                (:version + 1),
                :orderId,
                :transactionId,
                :status,
                :method,
                :amount,
                :createdAt,
                :updatedAt,
                :paidAt
                )
                """;

        final var aSqlPaymentDetails = """
                INSERT INTO payment_details (
                id,
                version,
                qr_code,
                qr_code_image_url,
                expires_in,
                payment_token,
                installments
                )
                VALUES (
                :id,
                (:version + 1),
                :qrCode,
                :qrCodeImageUrl,
                :expiresIn,
                :paymentToken,
                :installments
                )
                """;

        executeUpdate(aSql, aPayment);
        executeUpdatePaymentDetails(aSqlPaymentDetails, aPayment);
    }

    private void update(final Payment aPayment) {
        final var aSql = """
                UPDATE payments
                SET
                version = (:version + 1),
                order_id = :orderId,
                transaction_id = :transactionId,
                status = :status,
                method = :method,
                amount = :amount,
                created_at = :createdAt,
                updated_at = :updatedAt,
                paid_at = :paidAt
                WHERE id = :id AND version = :version
                """;

        final var aSqlPaymentDetails = """
                UPDATE payment_details
                SET
                version = (:version + 1),
                qr_code = :qrCode,
                qr_code_image_url = :qrCodeImageUrl,
                expires_in = :expiresIn,
                payment_token = :paymentToken,
                installments = :installments
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, aPayment) == 0) {
            throw ConflictException.with("Payment with identifier %s and version %d does not match, payment was updated by another transaction"
                    .formatted(aPayment.getId().value(), aPayment.getVersion()));
        }

        executeUpdatePaymentDetails(aSqlPaymentDetails, aPayment);
    }

    private int executeUpdate(final String aSqlStorePayment, final Payment aPayment) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aPayment.getId().value().toString());
        aParams.put("version", aPayment.getVersion());
        aParams.put("orderId", aPayment.getOrderId().value().toString());
        aParams.put("transactionId", aPayment.getTransactionId());
        aParams.put("status", aPayment.getStatus().name());
        aParams.put("method", aPayment.getMethod().name());
        aParams.put("amount", aPayment.getAmount());
        aParams.put("createdAt", aPayment.getCreatedAt());
        aParams.put("updatedAt", aPayment.getUpdatedAt());
        aParams.put("paidAt", aPayment.getPaidAt().orElse(null));

        return this.databaseClient.update(aSqlStorePayment, aParams);
    }

    private int executeUpdatePaymentDetails(final String aSql, final Payment aPayment) {
        final var aParamsPaymentDetails = new HashMap<String, Object>();
        aParamsPaymentDetails.put("id", aPayment.getId().value().toString());
        aParamsPaymentDetails.put("version", aPayment.getVersion());

        if (aPayment.getPaymentDetails().method().equals(PaymentMethod.PIX)) {
            aParamsPaymentDetails.put("qrCodeImageUrl", ((PixPaymentDetails) aPayment.getPaymentDetails()).getQrCodeImageUrl().orElse(null));
            aParamsPaymentDetails.put("qrCode", ((PixPaymentDetails) aPayment.getPaymentDetails()).getQrCode().orElse(null));
            aParamsPaymentDetails.put("expiresIn", ((PixPaymentDetails) aPayment.getPaymentDetails()).getExpiresIn());

            aParamsPaymentDetails.put("paymentToken", null);
            aParamsPaymentDetails.put("installments", 0);
        } else if (aPayment.getPaymentDetails().method().equals(PaymentMethod.CREDIT_CARD)) {
            aParamsPaymentDetails.put("paymentToken", ((CreditCardPaymentDetails) aPayment.getPaymentDetails()).paymentToken());
            aParamsPaymentDetails.put("installments", ((CreditCardPaymentDetails) aPayment.getPaymentDetails()).installments());

            aParamsPaymentDetails.put("qrCodeImageUrl", null);
            aParamsPaymentDetails.put("qrCode", null);
            aParamsPaymentDetails.put("expiresIn", 0);
        }

        return this.databaseClient.update(aSql, aParamsPaymentDetails);
    }

    private RowMap<Payment> paymentMapper() {
        return rs -> {
            final var aPaymentId = new PaymentID(ULID.fromString(rs.getString("id")));
            final var aVersion = rs.getLong("version");
            final var aOrderId = new OrderID(ULID.fromString(rs.getString("order_id")));
            final var aTransactionId = rs.getString("transaction_id");
            final var aStatus = PaymentStatus.from(rs.getString("status")).orElse(null);
            final var aMethod = PaymentMethod.from(rs.getString("method"));
            final var aAmount = rs.getBigDecimal("amount");
            final var aCreatedAt = JdbcUtils.getInstant(rs, "created_at");
            final var aUpdatedAt = JdbcUtils.getInstant(rs, "updated_at");
            final var aPaidAt = JdbcUtils.getInstant(rs, "paid_at");

            PaymentDetails aPaymentDetails = null;
            if (aMethod.isPresent() && aMethod.get().equals(PaymentMethod.PIX)) {
                aPaymentDetails = new PixPaymentDetails(
                        aAmount,
                        rs.getString("qr_code"),
                        rs.getString("qr_code_image_url"),
                        rs.getInt("expires_in")
                );
            } else if (aMethod.get().equals(PaymentMethod.CREDIT_CARD)) {
                aPaymentDetails = new CreditCardPaymentDetails(
                        aAmount,
                        "",
                        "",
                        "",
                        "",
                        rs.getString("payment_token"),
                        rs.getInt("installments")
                );
            }

            return Payment.with(
                    aPaymentId,
                    aVersion,
                    aOrderId,
                    aTransactionId,
                    aStatus,
                    aMethod.orElse(null),
                    aAmount,
                    aPaymentDetails,
                    aCreatedAt,
                    aUpdatedAt,
                    aPaidAt
            );
        };
    }
}
