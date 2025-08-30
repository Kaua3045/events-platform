package com.kaua.events.platform.infrastructure.payments;

import com.kaua.events.platform.application.repositories.PaymentRepository;
import com.kaua.events.platform.domain.payments.Payment;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

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
                qr_code,
                qr_code_image_url,
                created_at,
                updated_at,
                paid_at,
                expires_in
                )
                VALUES (
                :id,
                (:version + 1),
                :orderId,
                :transactionId,
                :status,
                :method,
                :amount,
                :qrCode,
                :qrCodeImageUrl,
                :createdAt,
                :updatedAt,
                :paidAt,
                :expiresIn
                )
                """;

        executeUpdate(aSql, aPayment);
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
                qr_code = :qrCode,
                qr_code_image_url = :qrCodeImageUrl,
                created_at = :createdAt,
                updated_at = :updatedAt,
                paid_at = :paidAt,
                expires_in = :expiresIn
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, aPayment) == 0) {
            throw ConflictException.with("Payment with identifier %s and version %d does not match, payment was updated by another transaction"
                    .formatted(aPayment.getId().value(), aPayment.getVersion()));
        }
    }

    private int executeUpdate(final String aSql, final Payment aPayment) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aPayment.getId().value().toString());
        aParams.put("version", aPayment.getVersion());
        aParams.put("orderId", aPayment.getOrderId().value().toString());
        aParams.put("transactionId", aPayment.getTransactionId());
        aParams.put("status", aPayment.getStatus().name());
        aParams.put("method", aPayment.getMethod().name());
        aParams.put("amount", aPayment.getAmount());
        aParams.put("qrCode", aPayment.getQrCode().orElse(null));
        aParams.put("qrCodeImageUrl", aPayment.getQrCodeImageUrl().orElse(null));
        aParams.put("createdAt", aPayment.getCreatedAt());
        aParams.put("updatedAt", aPayment.getUpdatedAt());
        aParams.put("paidAt", aPayment.getPaidAt().orElse(null));
        aParams.put("expiresIn", aPayment.getExpiresIn());

        return this.databaseClient.update(aSql, aParams);
    }
}
