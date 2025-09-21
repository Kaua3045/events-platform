package com.kaua.events.platform.infrastructure.gateways;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.infrastructure.configurations.annotations.InMemoryPaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class InMemoryPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(InMemoryPaymentGateway.class);

    // Armazena transactionId -> PaymentProcessResponse
    private final Map<String, PaymentProcessResponse> store = new ConcurrentHashMap<>();
    // Armazena transactionId -> orderId
    private final Map<String, String> transactionToOrder = new ConcurrentHashMap<>();

    private final WebClient webClient;
    private final Consumer<Runnable> webhookExecutor;

    public InMemoryPaymentGateway(@InMemoryPaymentClient final WebClient webClient) {
        this(webClient, r -> Thread.ofVirtual()
                .name("in-memory-payment-gateway-" + Thread.currentThread().threadId())
                .start(r));
    }

    public InMemoryPaymentGateway(@InMemoryPaymentClient final WebClient webClient,
                                  Consumer<Runnable> webhookExecutor) {
        this.webClient = Objects.requireNonNull(webClient);
        this.webhookExecutor = Objects.requireNonNull(webhookExecutor);
    }

    @Override
    public PaymentProcessResponse process(final PaymentProcessRequest request) {
        final var amount = request.paymentDetails().amount();
        final PaymentProcessStatus status;
        final String qrCode;
        final String qrCodeImageUrl;
        final int expiresIn;

        boolean simulatedValue = amount.compareTo(BigDecimal.ONE) >= 0 && amount.compareTo(new BigDecimal("50")) <= 0;
        if (request.paymentDetails().method().equals(PaymentMethod.PIX)) {
            status = simulatedValue
                    ? PaymentProcessStatus.ACTIVE
                    : PaymentProcessStatus.FAILED;

            qrCode = "FAKE-PIX-" + IdentifierUtils.generateNewIdWithoutHyphen();
            qrCodeImageUrl = "https://fake-pix.local/qrcode/" + request.transactionId();
            expiresIn = 3600;
        } else {
            status = simulatedValue
                    ? PaymentProcessStatus.WAITING
                    : PaymentProcessStatus.FAILED;

            qrCode = null;
            qrCodeImageUrl = null;
            expiresIn = 0;
        }

        final var response = new PaymentProcessResponse(qrCode, qrCodeImageUrl, expiresIn, status);

        store.putIfAbsent(request.transactionId(), response);
        transactionToOrder.putIfAbsent(request.transactionId(), request.orderId());

        log.info("[InMemoryPaymentGateway] Payment processed [orderId:{}] [transactionId:{}] [status:{}] [amount:{}]",
                request.orderId(), request.transactionId(), status, amount);

        if (request.paymentDetails().method().equals(PaymentMethod.PIX)) {
            simulatePixWebhook(request.transactionId(), request.orderId(), amount);
        } else {
            simulateCardWebhook(request.transactionId());
        }

        return response;
    }

    @Generated
    @Override
    public PaymentNotification getNotifications(final String notificationId) {
        final var orderId = transactionToOrder.get(notificationId);
        final var response = store.get(notificationId);

        log.info("[InMemoryPaymentGateway] Returning notification [notificationId:{}] [orderId:{}]", notificationId, orderId);

        return new PaymentNotification(
                200,
                List.of(
                        new PaymentNotificationData(
                                ThreadLocalRandom.current().nextLong(1, 1_000_000),
                                "PAYMENT",
                                orderId, // customId = orderId
                                response.status().name(),
                                1,
                                Instant.now().toString()
                        ),
                        new PaymentNotificationData(
                                ThreadLocalRandom.current().nextLong(1, 1_000_000),
                                "PAYMENT",
                                orderId, // customId = orderId
                                "approved",
                                1,
                                Instant.now().toString()
                        ),
                        new PaymentNotificationData(
                                ThreadLocalRandom.current().nextLong(1, 1_000_000),
                                "PAYMENT",
                                orderId, // customId = orderId
                                response.status().equals(PaymentProcessStatus.WAITING) ? "paid" : "unpaid",
                                1,
                                Instant.now().toString()
                        )
                )
        );
    }

    private void simulatePixWebhook(String transactionId, String orderId, BigDecimal amount) {
        webhookExecutor.accept(() -> {
            try {
                long delay = ThreadLocalRandom.current().nextLong(3000, 10000);
                sleep(delay);

                Map<String, Object> payload = Map.of(
                        "pix", List.of(
                                Map.of(
                                        "endToEndId", UUID.randomUUID().toString(),
                                        "txid", orderId,
                                        "customId", orderId,
                                        "valor", amount.toPlainString(),
                                        "horario", Instant.now().toString(),
                                        "infoPagador", "Teste de pagamento PIX in memory"
                                )
                        )
                );

                log.info("[InMemoryGateway] Sending PIX webhook [transactionId:{}] after {}ms -> {}", transactionId, delay, payload);

                webClient.post()
                        .uri("/webhooks/pix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnError(ex -> log.error("[InMemoryGateway] Error sending PIX webhook: {}", ex.getMessage(), ex))
                        .subscribe();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[InMemoryGateway] Virtual thread interrupted: {}", e.getMessage(), e);
            }
        });
    }

    private void simulateCardWebhook(String transactionId) {
        webhookExecutor.accept(() -> {
            try {
                long delay = ThreadLocalRandom.current().nextLong(2000, 7000);
                sleep(delay);

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("notification", transactionId);

                log.info("[InMemoryGateway] Sending card webhook [transactionId:{}] after {}ms -> {}", transactionId, delay, formData);

                webClient.post()
                        .uri("/webhooks/card/notification")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .bodyValue(formData)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnError(ex -> log.error("[InMemoryGateway] Error sending card webhook: {}", ex.getMessage(), ex))
                        .subscribe();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[InMemoryGateway] Virtual thread interrupted: {}", e.getMessage(), e);
            }
        });
    }

    public void clearStore() {
        store.clear();
        transactionToOrder.clear();
    }

    protected void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
