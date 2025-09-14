package com.kaua.events.platform.infrastructure.gateways;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.application.gateways.payment.PaymentCreditCardPaymentDetailsRequest;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiProperties;
import com.kaua.events.platform.infrastructure.gateways.helpers.ReactiveHttpClientUtils;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EfiPaymentGateway implements PaymentGateway, ReactiveHttpClientUtils {

    public static final String NAMESPACE_NAME = "payments";

    private static final Logger log = LoggerFactory.getLogger(EfiPaymentGateway.class);

    private final WebClient webClient;
    private final WebClient webClientCharges;
    private final GetClientCredentials getClientCredentials;
    private final GetClientCredentials getClientCredentialsCharges;
    private final EfiPixProperties efiPixProperties;
    private final EfiProperties efiProperties;
    private final Tracer tracer;

    public EfiPaymentGateway(
            @EfiPixClient final WebClient webClient,
            @EfiChargesClient final WebClient webClientCharges,
            @EfiPixClient final GetClientCredentials getClientCredentials,
            @EfiChargesClient final GetClientCredentials getClientCredentialsCharges,
            final EfiPixProperties efiPixProperties,
            final EfiProperties efiProperties,
            final Tracer tracer
    ) {
        this.webClient = Objects.requireNonNull(instrument(webClient));
        this.webClientCharges = Objects.requireNonNull(instrument(webClientCharges));
        this.getClientCredentials = Objects.requireNonNull(getClientCredentials);
        this.getClientCredentialsCharges = Objects.requireNonNull(getClientCredentialsCharges);
        this.efiPixProperties = Objects.requireNonNull(efiPixProperties);
        this.efiProperties = Objects.requireNonNull(efiProperties);
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
            log.info("Starting EFI payment [orderId={}] [transactionId={}] [amount={}]",
                    request.orderId(), request.transactionId(), request.paymentDetails().amount());
            final var aToken = this.getClientCredentials.retrieve();
            return processPixPayment(request, aToken);
        }
        final var aToken = this.getClientCredentialsCharges.retrieve();
        return processCreditCardPayment(request, aToken);
    }

    @Override
    public PaymentNotification getNotifications(final String notificationId) {
        log.debug("Getting EFI payment notification [notificationId={}]", notificationId);
        final var aOutput = doGet(notificationId, () -> webClientCharges.get()
                .uri("/v1/notification/" + notificationId) // TODO validate this
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.getClientCredentialsCharges.retrieve())
                .retrieve()
                .onStatus(is5xx, a5xxHandler(notificationId, "get notification"))
                .onStatus(isTooManyRequests, tooManyRequestsHandler(notificationId, "get notification"))
                .onStatus(isNotFound, notFoundHandler(notificationId))
                .bodyToMono(NotificationResponse.class)
                .block())
                .orElseThrow(() -> NotFoundException.with("Notification %s does not found".formatted(notificationId)));

        log.info("EFI payment notification received [notificationId={}] [code={}] [dataSize={}]",
                notificationId, aOutput.code(), aOutput.data().size());


        return new PaymentNotification(
                aOutput.code(),
                aOutput.data().stream().map(n -> new PaymentNotificationData(
                        n.id(),
                        n.type(),
                        n.customId(),
                        n.status().current(),
                        n.identifiers().chargeId(),
                        n.createdAt()
                )).toList()
        );
    }

    private PaymentProcessResponse processCreditCardPayment(final PaymentProcessRequest request, final String aToken) {
        final var aPaymentDetails = (PaymentCreditCardPaymentDetailsRequest) request.paymentDetails();

        final var aChargePay = doPost(request.transactionId(), () -> webClientCharges.post()
                .uri("/v1/charge/one-step")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aToken))
                .bodyValue(Map.of(
                        "items", List.of(Map.of(
                                "name", "Event Ticket",
                                "value", request.paymentDetails().amount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue(),
                                "amount", 1
                        )),
                        "metadata", Map.of(
                                "custom_id", request.orderId(),
                                "notification_url", getNotificationUrl().concat("/api/webhooks/card/notification")
                        ),
                        "payment", Map.of(
                                "credit_card",
                                Map.of("customer", Map.of(
                                                "name", aPaymentDetails.name(),
                                                "cpf", aPaymentDetails.documentNumber(),
                                                "email", aPaymentDetails.email(),
                                                "phone_number", aPaymentDetails.phoneNumber()
                                        ),
                                        "installments", aPaymentDetails.installments(),
                                        "payment_token", aPaymentDetails.paymentToken())
                        )

                ))
                .retrieve()
                .onStatus(isBadRequest, badRequestHandler(request.orderId(), "process payment"))
                .onStatus(isConflict, conflictHandler(request.orderId(), "process payment"))
                .onStatus(isTooManyRequests, tooManyRequestsHandler(request.orderId(), "process payment"))
                .onStatus(is5xx, a5xxHandler(request.orderId(), "process payment"))
                .bodyToMono(ChargeResponse.class)
                .block();

        log.debug("EFI Credit Card charge create response received [orderId={}] [transactionId={}] [chargeId={}] [status={}] [code={}]",
                request.orderId(), request.transactionId(), aChargePay.data().chargeId(), aChargePay.data().status(), aChargePay.code());

        if (aChargePay.data().status().equalsIgnoreCase("unpaid")) {
            log.warn("EFI Credit Card charge pay failed [orderId={}] [transactionId={}] [chargeId={}] [status={}] [amount={}]",
                    request.orderId(), request.transactionId(), aChargePay.data().chargeId(),
                    aChargePay.data().status(), request.paymentDetails().amount());
            // TODO handle
//            {
//                "code": 200, // retorno HTTP "200" informando que o pedido foi bem sucedido
//                    "data": {
//                "installments": 1, // número de parcelas em que o pagamento deve ser dividido
//                        "installment_value": 5990, // valor da parcela. Por exemplo: 8900 (equivale a R$ 89,00)
//                        "charge_id": numero_charge_id, // número da ID referente à transação gerada
//                        "status": "unpaid", // Indica que o pagamento foi reprovado.
//                        "refusal": {
//                    "reason": "Sistema de segurança: Os dados e comportamentos de utilização do cartão se assemelham a práticas e cenários de alto risco para pagamentos online. Utilize outro cartão ou outro meio de pagamento.", // Mensagem que contém o motivo da recusa da transação.
//                            "retry": true // Indica se é possível tentar novamente a transação.
//                },
//                "total": 5990, // valor, em centavos. Por exemplo: 8900 (equivale a R$ 89,00)
//                        "payment": "credit_card" // forma de pagamento associada à esta transação ("credit_card" equivale a "cartão de crédito")
//            }
//            }
            return new PaymentProcessResponse(null, null, 0, PaymentProcessStatus.FAILED);
        }

        log.info("EFI Credit Card charge pay processed [orderId={}] [transactionId={}] [chargeId={}] [status={}] [amount={}]",
                request.orderId(), request.transactionId(), aChargePay.data().chargeId(),
                aChargePay.data().status(), request.paymentDetails().amount());

        return new PaymentProcessResponse(null, null, 0, PaymentProcessStatus.WAITING);
    }

    private PaymentProcessResponse processPixPayment(final PaymentProcessRequest request, final String aToken) {
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

    @Generated
    private String getNotificationUrl() {
        if (this.efiProperties.isNgrok()) {
            final var response = WebClient.create().get()
                    .uri(this.efiProperties.getNgrokUrl().concat("/api/tunnels"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> tunnels = (List<Map<String, Object>>) response.get("tunnels");
            for (Map<String, Object> tunnel : tunnels) {
                if ("https".equals(tunnel.get("proto"))) {
                    return (String) tunnel.get("public_url");
                }
            }
            throw new IllegalStateException("Ngrok HTTPS tunnel not found");
        }
        return this.efiProperties.getNotificationUrl();
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChargeResponse(
            @JsonProperty("code") int code,
            @JsonProperty("data") ChargeDataResponse data
    ) {
    }

    record ChargeDataResponse(
            @JsonProperty("installments") int installments,
            @JsonProperty("installment_value") int installmentValue,
            @JsonProperty("payment") String payment,
            @JsonProperty("charge_id") String chargeId,
            @JsonProperty("status") String status,
            @JsonProperty("total") int total,
            @JsonProperty("custom_id") String customId,
            @JsonProperty("created_at") String createdAt
    ) {
    }

    record NotificationResponse(
            @JsonProperty("code") int code,
            @JsonProperty("data") List<NotificationData> data
    ) {
    }

    record NotificationData(
            @JsonProperty("id") long id,
            @JsonProperty("type") String type,
            @JsonProperty("custom_id") String customId,
            @JsonProperty("status") Status status,
            @JsonProperty("identifiers") Identifiers identifiers,
            @JsonProperty("created_at") String createdAt
    ) {
    }

    record Status(
            @JsonProperty("current") String current,
            @JsonProperty("previous") String previous
    ) {
    }

    record Identifiers(
            @JsonProperty("carnet_id") Long carnetId,
            @JsonProperty("charge_id") Long chargeId
    ) {
    }
}
