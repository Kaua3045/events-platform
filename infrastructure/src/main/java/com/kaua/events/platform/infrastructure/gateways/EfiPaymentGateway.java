package com.kaua.events.platform.infrastructure.gateways;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import com.kaua.events.platform.infrastructure.gateways.helpers.ReactiveHttpClientUtils;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EfiPaymentGateway implements PaymentGateway, ReactiveHttpClientUtils {

    public static final String NAMESPACE_NAME = "payments";

    private static final Logger log = LoggerFactory.getLogger(EfiPaymentGateway.class);

    private final WebClient webClient;
    private final GetClientCredentials getClientCredentials;
    private final EfiPixProperties efiPixProperties;
    private final Tracer tracer;

    public EfiPaymentGateway(
            @EfiClient final WebClient webClient,
            final GetClientCredentials getClientCredentials,
            final EfiPixProperties efiPixProperties,
            final Tracer tracer
    ) {
        this.webClient = Objects.requireNonNull(instrument(webClient));
        this.getClientCredentials = Objects.requireNonNull(getClientCredentials);
        this.efiPixProperties = Objects.requireNonNull(efiPixProperties);
        this.tracer = Objects.requireNonNull(tracer);
    }

    // TODO poderia ter retry e em caso de error absoluto, teriamos 2 opcoes, notificar que foi error interno o pix
    // e tentar reprocessar com um job, ou fazer alguma outra coisa
    @Bulkhead(name = NAMESPACE_NAME)
    @CircuitBreaker(name = NAMESPACE_NAME)
    @Retry(name = NAMESPACE_NAME)
    @Override
    public PaymentProcessResponse process(final PaymentProcessRequest request) {
        if (request.paymentDetails().method().equals(PaymentMethod.PIX)) {
            log.info("Starting EFI Pix payment [orderId={}] [transactionId={}] [amount={}]",
                    request.orderId(), request.transactionId(), request.paymentDetails().amount());

            final var aToken = this.getClientCredentials.retrieve();

            final var aOutput = doUpdate(request.orderId(), () -> webClient.put()
                    .uri("/v2/cob/" + request.transactionId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aToken)
                    .header("x-idempotency-key", request.orderId())
                    .bodyValue(Map.of(
                            "calendario", Map.of("expiracao", 3600),
                            "valor", Map.of("original", request.paymentDetails().amount().toPlainString()),
                            "chave", this.efiPixProperties.getPixKeys().getFirst(),
                            "infoAdicionais", List.of(
                                    Map.of("nome", "orderId", "valor", request.orderId())
                            )
                    ))
                    .retrieve()
                    .onStatus(isBadRequest, badRequestHandler(request.orderId(), "process payment"))
                    .onStatus(isConflict, conflictHandler(request.orderId(), "process payment"))
                    .onStatus(isTooManyRequests, tooManyRequestsHandler(request.orderId(), "process payment"))
                    .onStatus(is5xx, a5xxHandler(request.orderId(), "process payment"))
                    .bodyToMono(EfiCreatePixResponse.class)
                    .block());

            log.debug("EFI Pix PUT response received [orderId={}] [txId={}] [status={}] [expiresIn={}s]",
                    request.orderId(), aOutput.txId(), aOutput.status(), aOutput.calendario().expiracao());

            final var aOutputTwo = doGet(request.orderId(), () -> webClient.get()
                    .uri("/v2/loc/" + aOutput.loc().id() + "/qrcode")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aToken)
                    .retrieve()
                    .onStatus(isBadRequest, notFoundHandler(request.orderId()))
                    .bodyToMono(EfiCreateQrCodePixResponse.class)
                    .block()).orElseThrow(() -> NotFoundException.with("QrCode location does not found"));

            log.debug("EFI Pix QRCode response received [orderId={}] [qrCodeUrl={}]",
                    request.orderId(), aOutputTwo.qrCodeImageUrl());

            log.info("EFI Pix payment created successfully [orderId={}] [transactionId={}] [status={}] [expiresIn={}s] [amount={}]",
                    request.orderId(), aOutput.txId(), aOutput.status(),
                    aOutput.calendario().expiracao(), request.paymentDetails().amount());

            return new PaymentProcessResponse(
                    aOutputTwo.qrCode(),
                    aOutputTwo.qrCodeImageUrl(),
                    aOutput.calendario().expiracao(),
                    PaymentProcessStatus.ACTIVE
            );
        }
        return null;
    }

    @Override
    public String namespace() {
        return NAMESPACE_NAME;
    }

    @Override
    public Logger logger() {
        return log;
    }

    @Override
    public Tracer tracer() {
        return tracer;
    }

    record EfiCreateQrCodePixResponse(
            @JsonProperty("qrcode") String qrCode,
            @JsonProperty("imagemQrcode") String qrCodeImage,
            @JsonProperty("linkVisualizacao") String qrCodeImageUrl
    ) {
    }

    record EfiCreatePixResponse(
            @JsonProperty("calendario") Calendario calendario,
            @JsonProperty("txid") String txId,
            @JsonProperty("loc") Location loc,
            @JsonProperty("status") String status,
            @JsonProperty("pixCopiaECola") String pixCopiaECola
    ) {
    }

    record Location(
            @JsonProperty("id") int id
    ) {
    }

    record Calendario(
            @JsonProperty("criacao") Instant criacao,
            @JsonProperty("expiracao") int expiracao
    ) {
    }
}
