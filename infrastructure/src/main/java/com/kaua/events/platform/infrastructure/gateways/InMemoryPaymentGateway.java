package com.kaua.events.platform.infrastructure.gateways;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.infrastructure.configurations.annotations.InMemoryPaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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

    private final Map<String, PaymentProcessResponse> store = new ConcurrentHashMap<>();

    private final WebClient webClient;
    private final Consumer<Runnable> webhookExecutor;

    public InMemoryPaymentGateway(@InMemoryPaymentClient final WebClient webClient) {
        this(
                webClient,
                r -> Thread.ofVirtual()
                        .name("in-memory-payment-gateway-" + Thread.currentThread().threadId())
                        .start(r)
        );
    }

    public InMemoryPaymentGateway(
            @InMemoryPaymentClient final WebClient webClient,
            Consumer<Runnable> webhookExecutor
    ) {
        this.webClient = Objects.requireNonNull(webClient);
        this.webhookExecutor = Objects.requireNonNull(webhookExecutor);
    }

    @Override
    public PaymentProcessResponse process(final PaymentProcessRequest request) {
        if (!request.paymentDetails().method().equals(PaymentMethod.PIX)) {
            throw new UnsupportedOperationException("Only PIX is supported in local in-memory gateway");
        }

        final var aAmount = request.paymentDetails().amount();
        final PaymentProcessStatus status;

        if (aAmount.compareTo(BigDecimal.ONE) >= 0 && aAmount.compareTo(new BigDecimal("50")) <= 0) {
            status = PaymentProcessStatus.ACTIVE;
        } else {
            status = PaymentProcessStatus.FAILED;
        }

        final var qrCode = "FAKE-PIX-" + IdentifierUtils.generateNewIdWithoutHyphen();
        final var qrCodeImageUrl = "https://fake-pix.local/qrcode/" + request.transactionId();

        final var aResponse = new PaymentProcessResponse(
                qrCode,
                qrCodeImageUrl,
                3600,
                status
        );

        final var aStoreResult = this.store.putIfAbsent(request.transactionId(), aResponse);

        if (aStoreResult != null) {
            log.warn("[InMemoryPaymentGateway] Payment already exists [orderId:{}] [transactionId:{}] [status:{}] [expiresIn:{}] [amount:{}]",
                    request.orderId(),
                    request.transactionId(),
                    aStoreResult.status(),
                    aStoreResult.expiresIn(),
                    request.paymentDetails().amount()
            );

            return aStoreResult;
        }

        log.info("[InMemoryPaymentGateway] Payment processed [orderId:{}] [transactionId:{}] [status:{}] [expiresIn:{}] [amount:{}]",
                request.orderId(),
                request.transactionId(),
                aResponse.status(),
                aResponse.expiresIn(),
                request.paymentDetails().amount()
        );

        simulateWebhookCallback(request.transactionId(), aAmount);

        return aResponse;
    }

    private void simulateWebhookCallback(
            final String aTransactionId,
            final BigDecimal aAmount
    ) {
        webhookExecutor.accept(() -> {
            try {
                long delay = ThreadLocalRandom.current().nextLong(3000, 10000);
                sleep(delay);

                Map<String, Object> pixItem = Map.of(
                        "endToEndId", UUID.randomUUID().toString(), // gera id único
                        "txid", aTransactionId,
                        "chave", "71cdf9ba-c695-4e3c-b010-abb521a3f1be", // pode vir de props
                        "valor", aAmount.toPlainString(),
                        "horario", Instant.now().toString(),
                        "infoPagador", "Teste de pagamento em ambiente in memory",
                        "gnExtras", Map.of(
                                "pagador", Map.of(
                                        "nome", "Teste in memory",
                                        "cnpj", "09089356000118",
                                        "codigoBanco", "09089356"
                                )
                        )
                );

                Map<String, Object> payload = Map.of(
                        "pix", List.of(pixItem)
                );

                log.info("[InMemoryGateway] [transactionId:{}] Sending fake webhook callback after {}ms -> {}", aTransactionId, delay, payload);

                webClient.post()
                        .uri("/webhooks/pix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnError(ex -> log.error("[InMemoryGateway] [transactionId:{}] Error sending webhook: {}", aTransactionId, ex.getMessage(), ex))
                        .subscribe();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[InMemoryGateway] [transactionId:{}] Virtual thread interrupted: {}", aTransactionId, e.getMessage(), e);
            }
        });
    }

    public void clearStore() {
        this.store.clear();
    }

    protected void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
