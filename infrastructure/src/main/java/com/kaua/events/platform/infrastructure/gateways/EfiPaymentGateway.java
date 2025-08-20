package com.kaua.events.platform.infrastructure.gateways;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.domain.payments.PaymentMethod;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class EfiPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(EfiPaymentGateway.class);

    private final WebClient webClient;
    private final GetClientCredentials getClientCredentials;

    public EfiPaymentGateway(
            @EfiClient final WebClient webClient,
            final GetClientCredentials getClientCredentials
    ) {
        this.webClient = Objects.requireNonNull(webClient);
        this.getClientCredentials = Objects.requireNonNull(getClientCredentials);
    }

    // TODO poderia ter retry e em caso de error absoluto, teriamos 2 opcoes, notificar que foi error interno o pix
    // e tentar reprocessar com um job, ou fazer alguma outra coisa
    @Override
    public PaymentProcessResponse process(final PaymentProcessRequest request) {
        if (request.paymentDetails().method().equals(PaymentMethod.PIX)) {
            log.debug("Creating efi pix payment {}", request);
            final var aToken = this.getClientCredentials.retrieve();

            final var aOutput = this.webClient.put()
                    .uri("/v2/cob/" + request.transactionId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aToken)
                    .bodyValue(Map.of(
                            "calendario", Map.of("expiracao", 3600),
                            "devedor", Map.of(), // TODO NOME E CPF DO DEVEDOR,
                            "valor", Map.of("original", request.paymentDetails().amount()),
                            "chave", "", // TODO Chave tecnicamente pode vir das props
                            "infoAdicionais", Map.of("nome", "orderId", "valor", request.orderId())
                    ))
                    .retrieve()
                    .bodyToMono(EfiCreatePixResponse.class)
                    .block();

            log.info("EfiPix created successfully [orderId:{}] [transactionId:{}] [status:{}] [expiresIn:{}]",
                    request.orderId(),
                    aOutput.txId(),
                    aOutput.status(),
                    aOutput.calendario().expiracao());

            return new PaymentProcessResponse(
                    aOutput.pixCopiaECola(),
                    aOutput.locationQrCodeImage(),
                    aOutput.calendario().expiracao(),
                    // TODO in future make switch and map status
                    PaymentProcessStatus.ACTIVE
            );
        }
        return null;
    }

    record EfiCreatePixResponse(
            @JsonProperty("calendario") Calendario calendario,
            @JsonProperty("txid") String txId,
            @JsonProperty("location") String locationQrCodeImage,
            @JsonProperty("status") String status,
            @JsonProperty("pixCopiaECola") String pixCopiaECola
    ) {
    }

    record Calendario(
            @JsonProperty("criacao") Instant criacao,
            @JsonProperty("expiracao") int expiracao
    ) {
    }
}
