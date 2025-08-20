package com.kaua.events.platform.infrastructure.gateways;

import com.kaua.events.platform.AbstractRestClientTest;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessRequest;
import static com.kaua.events.platform.application.gateways.PaymentGateway.PaymentProcessStatus;

class EfiPaymentGatewayTest extends AbstractRestClientTest {

    @Autowired
    private EfiPaymentGateway gateway;

    @MockitoSpyBean
    private GetClientCredentials getClientCredentials;

    @Test
    void givenAValidPixRequest_whenProcess_thenShouldReturnPixResponse() {
        final var transactionId = "tx123";
        final var orderId = "order123";
        final var amount = new BigDecimal("100.00");
        final var aToken = "accessToken";

        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        stubFor(get(urlPathEqualTo("/credentials"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"token\":\"" + aToken + "\"}")));

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "calendario": {"criacao":"2025-08-20T10:00:00Z","expiracao":3600},
                                  "txid":"%s",
                                  "location":"http://qrcode.url",
                                  "status":"ACTIVE",
                                  "pixCopiaECola":"000201...123"
                                }
                                """.formatted(transactionId))));

        final var request = new PaymentProcessRequest(
                transactionId,
                orderId,
                new PixPaymentDetails(amount)
        );

        final var response = this.gateway.process(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("000201...123", response.qrCode());
        Assertions.assertEquals("http://qrcode.url", response.qrCodeImageUrl());
        Assertions.assertEquals(3600, response.expiresIn());
        Assertions.assertEquals(PaymentProcessStatus.ACTIVE, response.status());
    }

    @Test
    void givenPixRequestButEfiIsDown_whenProcess_thenShouldThrowInternalErrorException() {
        final var transactionId = "tx123";
        final var orderId = "order123";
        final var amount = new BigDecimal("100.00");
        final var aToken = "accessToken";

        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        stubFor(get(urlPathEqualTo("/credentials"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"token\":\"" + aToken + "\"}")));

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse().withStatus(500)));

        final var request = new PaymentProcessRequest(
                transactionId,
                orderId,
                new PixPaymentDetails(amount)
        );

        final var exception = Assertions.assertThrows(WebClientResponseException.class, () -> this.gateway.process(request));

        Assertions.assertTrue(exception.getMessage().contains("500 Internal Server Error from PUT"));
    }

    @Test
    void givenNonPixPayment_whenProcess_thenShouldReturnNull() {
        final var transactionId = "tx123";
        final var orderId = "order123";
        final var amount = new BigDecimal("100.00");

        final var request = new PaymentProcessRequest(
                transactionId,
                orderId,
                new CreditCardPaymentDetails(amount)
        );

        final var response = this.gateway.process(request);

        Assertions.assertNull(response, "Expected null for non-PIX payments");
    }
}
