package com.kaua.events.platform.infrastructure.gateways;

import com.kaua.events.platform.AbstractRestClientTest;
import com.kaua.events.platform.ObservationTest;
import com.kaua.events.platform.application.gateways.payment.PaymentCreditCardPaymentDetailsRequest;
import com.kaua.events.platform.application.gateways.payment.PaymentPixPaymentDetailsRequest;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiProperties;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import com.kaua.events.platform.infrastructure.exceptions.TooManyRequestsException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

class EfiPaymentGatewayTest extends AbstractRestClientTest implements ObservationTest {

    @Autowired
    private EfiPaymentGateway gateway;

    @MockitoSpyBean
    @EfiPixClient
    private GetClientCredentials getClientCredentials;

    @MockitoSpyBean
    @EfiChargesClient
    private GetClientCredentials getChargesClientCredentials;

    @Autowired
    private EfiPixProperties efiPixProperties;

    @Autowired
    private EfiProperties efiProperties;

    @Autowired
    private InMemorySpanExporter spanExporter;

    @BeforeEach
    void setup() {
        resetSpans();
    }

    @Test
    void givenAValidPixRequest_whenProcess_thenShouldReturnPixResponse() {
        final var transactionId = "tx123";
        final var orderId = "order123";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token123";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader("x-idempotency-key", equalTo(orderId))
                .withRequestBody(containing("\"original\":\"100.00\""))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "calendario": {"criacao":"2025-08-20T10:00:00Z","expiracao":3600},
                                  "txid":"%s",
                                  "loc":{"id":321},
                                  "status":"ATIVA",
                                  "pixCopiaECola":"000201...123"
                                }
                                """.formatted(transactionId))));

        stubFor(get(urlPathEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "qrcode":"000201...123",
                                  "imagemQrcode":"data:image/png;base64,AAA...",
                                  "linkVisualizacao":"http://qrcode.url"
                                }
                                """)));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));
        final var response = this.gateway.process(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("000201...123", response.qrCode());
        Assertions.assertEquals("http://qrcode.url", response.qrCodeImageUrl());
        Assertions.assertEquals(3600, response.expiresIn());
        Assertions.assertEquals(EfiPaymentGateway.PaymentProcessStatus.ACTIVE, response.status());

        verify(1, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader("x-idempotency-key", equalTo(orderId)));
        verify(1, getRequestedFor(urlEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken)));
    }

    @Test
    void givenPixRequestButEfiReturns5xx_whenProcess_thenShouldThrowInternalErrorException() {
        final var transactionId = "tx500";
        final var orderId = "order500";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token5xx";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(InternalErrorException.class,
                () -> this.gateway.process(request));
        Assertions.assertTrue(exception.getMessage().contains("Error observed"));

        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))); // retry
    }

    @Test
    void givenPixRequestButEfiReturns400_whenProcess_thenShouldThrowDomainException() {
        final var transactionId = "tx422";
        final var orderId = "order422";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token422";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        final var expectedErrorMessage = "A chave informada não faz referência à conta Efí autenticada";
        final var expectedErrorProperty = "chave_invalida";

        final var body = Map.of(
                "nome", expectedErrorProperty,
                "mensagem", expectedErrorMessage
        );

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(writeValueAsString(body))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> this.gateway.process(request));

        Assertions.assertEquals("ValidationException", exception.getMessage());
        Assertions.assertEquals(expectedErrorMessage, exception.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, exception.getErrors().getFirst().property());

        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))); // TODO erro de validacao nao dar retry
    }

    @Test
    void givenPixRequestButEfiReturns409_whenProcess_thenShouldThrowConflictException() {
        final var transactionId = "tx422";
        final var orderId = "order422";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token422";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        final var expectedErrorProperty = "txid_duplicado";
        final var expectedErrorMessage = "Campo txid informado já foi utilizado em outra cobrança";

        final var body = Map.of(
                "nome", expectedErrorProperty,
                "mensagem", expectedErrorMessage
        );

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(writeValueAsString(body))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(ConflictException.class,
                () -> this.gateway.process(request));

        Assertions.assertEquals("ConflictException", exception.getMessage());
        Assertions.assertEquals(expectedErrorMessage, exception.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, exception.getErrors().getFirst().property());

        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))); // TODO erro de validacao nao dar retry
    }

    @Test
    void givenPixRequestButEfiReturns429_whenProcess_thenShouldThrowTooManyRequestsException() {
        final var transactionId = "tx422";
        final var orderId = "order422";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token422";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        final var expectedErrorProperty = "Limite de Requisições Excedido";
        final var expectedErrorMessage = "As requisições estão temporariamente limitadas. Aguarde um momento e tente novamente";

        final var body = Map.of(
                "nome", expectedErrorProperty,
                "mensagem", expectedErrorMessage
        );

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(writeValueAsString(body))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(TooManyRequestsException.class,
                () -> this.gateway.process(request));

        Assertions.assertEquals("TooManyRequestsException", exception.getMessage());
        Assertions.assertEquals(expectedErrorMessage, exception.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, exception.getErrors().getFirst().property());

        verify(1, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))); // TODO erro de validacao nao dar retry
    }

    @Test
    void givenTimeoutExceed_whenProcess_thenShouldThrowInternalErrorException() {
        final var transactionId = "txTimeout";
        final var orderId = "orderTimeout";
        final var amount = BigDecimal.valueOf(50);
        final var aToken = "tokenTimeout";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withFixedDelay(2000)));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(InternalErrorException.class,
                () -> this.gateway.process(request));

        Assertions.assertTrue(exception.getMessage().contains("Timeout"));
        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))); // retry
    }

    @Test
    void givenBulkheadIsFull_whenProcess_thenShouldThrowBulkheadFullException() {
        final var aToken = "tokenBulkhead";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        acquireBulkheadPermission(EfiPaymentGateway.NAMESPACE_NAME);

        final var request = new EfiPaymentGateway.PaymentProcessRequest("tx123", "order123", new PaymentPixPaymentDetailsRequest(BigDecimal.TEN));
        final var exception = Assertions.assertThrows(BulkheadFullException.class,
                () -> this.gateway.process(request));

        Assertions.assertTrue(exception.getMessage().contains("is full"));
        releaseBulkheadPermission(EfiPaymentGateway.NAMESPACE_NAME);
    }

    @Test
    void givenCircuitBreakerIsOpen_whenProcess_thenShouldThrowCallNotPermittedException() {
        final var aToken = "tokenCB";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        transitionToOpenState(EfiPaymentGateway.NAMESPACE_NAME);

        final var request = new EfiPaymentGateway.PaymentProcessRequest("tx123", "order123", new PaymentPixPaymentDetailsRequest(BigDecimal.TEN));
        final var exception = Assertions.assertThrows(CallNotPermittedException.class,
                () -> this.gateway.process(request));

        Assertions.assertTrue(exception.getMessage().contains("OPEN"));
        checkCircuitBreakerState(EfiPaymentGateway.NAMESPACE_NAME, CircuitBreaker.State.OPEN);
    }

    @Test
    void givenAValidPixRequestButFailGetQrCodeLocation_whenProcess_thenShouldThrowNotFoundException() {
        final var transactionId = "tx123";
        final var orderId = "order123";
        final var amount = new BigDecimal("100.00");
        final var aToken = "token123";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        final var expectedErrorMessage = "QrCode location does not found";

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader("x-idempotency-key", equalTo(orderId))
                .withRequestBody(containing("\"original\":\"100.00\""))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "calendario": {"criacao":"2025-08-20T10:00:00Z","expiracao":3600},
                                  "txid":"%s",
                                  "loc":{"id":321},
                                  "status":"ATIVA",
                                  "pixCopiaECola":"000201...123"
                                }
                                """.formatted(transactionId))));

        stubFor(get(urlPathEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(writeValueAsString(Map.of(
                                "nome", "location_nao_encontrada",
                                "mensagem", "Nenhuma location encontrado para o identificador informado"
                        )))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        final var exception = Assertions.assertThrows(NotFoundException.class,
                () -> this.gateway.process(request));

        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());

        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader("x-idempotency-key", equalTo(orderId)));
        verify(2, getRequestedFor(urlEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken)));
    }

    @Test
    void givenPixRequestFailsInitially_whenProcess_thenRetryAndSucceed() {
        final var transactionId = "txRetry";
        final var orderId = "orderRetry";
        final var amount = new BigDecimal("100.00");
        final var aToken = "tokenRetry";
        Mockito.doReturn(aToken).when(getClientCredentials).retrieve();

        final var request = new EfiPaymentGateway.PaymentProcessRequest(transactionId, orderId, new PaymentPixPaymentDetailsRequest(amount));

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .inScenario("RetryScenario")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("SecondTry")
                .willReturn(aResponse()
                        .withStatus(500)));

        stubFor(put(urlPathEqualTo("/v2/cob/" + transactionId))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("SecondTry")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "calendario": {"criacao":"2025-08-20T10:00:00Z","expiracao":3600},
                                  "txid":"%s",
                                  "loc":{"id":321},
                                  "status":"ATIVA",
                                  "pixCopiaECola":"000201...retry"
                                }
                                """.formatted(transactionId))));

        stubFor(get(urlPathEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "qrcode":"000201...retry",
                                  "imagemQrcode":"data:image/png;base64,AAA...",
                                  "linkVisualizacao":"http://qrcode.retry.url"
                                }
                                """)));

        final var response = this.gateway.process(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("000201...retry", response.qrCode());
        Assertions.assertEquals("http://qrcode.retry.url", response.qrCodeImageUrl());
        Assertions.assertEquals(3600, response.expiresIn());
        Assertions.assertEquals(EfiPaymentGateway.PaymentProcessStatus.ACTIVE, response.status());

        verify(2, putRequestedFor(urlEqualTo("/v2/cob/" + transactionId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .withHeader("x-idempotency-key", equalTo(orderId)));
        verify(1, getRequestedFor(urlEqualTo("/v2/loc/321/qrcode"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken)));
    }

    @Test
    void givenValidCreditCardRequest_whenProcess_thenShouldReturnWaitingStatus() {
        final var transactionId = "txCard123";
        final var orderId = "orderCard123";
        final var amount = BigDecimal.valueOf(59.90);
        final var aToken = "tokenCard123";

        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(post(urlPathEqualTo("/v1/charge/one-step"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "code": 200,
                                  "data": {
                                    "installments": 1,
                                    "installment_value": 5990,
                                    "payment": "credit_card",
                                    "charge_id": "charge123",
                                    "status": "paid",
                                    "total": 5990,
                                    "custom_id": "%s",
                                    "created_at": "2025-08-30T10:00:00Z"
                                  }
                                }
                                """.formatted(transactionId))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(
                transactionId,
                orderId,
                new PaymentCreditCardPaymentDetailsRequest(
                        amount,
                        "John Doe",
                        "123.456.789-00",
                        "cpf",
                        "john.doe@mail.com",
                        "+55 (11) 91234-5678",
                        "120834182789",
                        1
                )
        );

        final var response = this.gateway.process(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(EfiPaymentGateway.PaymentProcessStatus.WAITING, response.status());

        verify(1, postRequestedFor(urlEqualTo("/v1/charge/one-step"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken)));
    }

    @Test
    void givenCreditCardPaymentUnpaid_whenProcess_thenShouldReturnFailedStatus() {
        final var transactionId = "txCardUnpaid";
        final var orderId = "orderCardUnpaid";
        final var amount = BigDecimal.valueOf(89.00);
        final var aToken = "tokenCardUnpaid";

        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(post(urlPathEqualTo("/v1/charge/one-step"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "code": 200,
                                  "data": {
                                    "installments": 1,
                                    "installment_value": 8900,
                                    "payment": "credit_card",
                                    "charge_id": "chargeUnpaid",
                                    "status": "unpaid",
                                    "total": 8900,
                                    "custom_id": "%s",
                                    "created_at": "2025-08-30T10:00:00Z"
                                  }
                                }
                                """.formatted(transactionId))));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(
                transactionId,
                orderId,
                new PaymentCreditCardPaymentDetailsRequest(
                        amount,
                        "Jane Doe",
                        "987.654.321-00",
                        "cpf",
                        "jane.doe@mail.com",
                        "+55 (11) 91234-5678",
                        "987654321234",
                        1
                )
        );

        final var response = this.gateway.process(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(EfiPaymentGateway.PaymentProcessStatus.FAILED, response.status());

        verify(1, postRequestedFor(urlEqualTo("/v1/charge/one-step")));
    }

    @Test
    void givenCreditCardPaymentWithServerError_whenProcess_thenShouldThrowInternalErrorException() {
        final var transactionId = "txCard500";
        final var orderId = "orderCard500";
        final var amount = BigDecimal.valueOf(120);
        final var aToken = "tokenCard500";

        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(post(urlPathEqualTo("/v1/charge/one-step"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        final var request = new EfiPaymentGateway.PaymentProcessRequest(
                transactionId,
                orderId,
                new PaymentCreditCardPaymentDetailsRequest(
                        amount,
                        "Alice",
                        "111.222.333-44",
                        "cpf",
                        "alice@mail.com",
                        "+55 (11) 91234-5678",
                        "123456789012",
                        1
                )
        );

        final var exception = Assertions.assertThrows(InternalErrorException.class,
                () -> this.gateway.process(request));

        Assertions.assertTrue(exception.getMessage().contains("Error observed"));

        verify(2, postRequestedFor(urlEqualTo("/v1/charge/one-step"))); // retry
    }

    @Test
    void givenValidNotificationId_whenGetNotifications_thenReturnPaymentNotification() {
        final var notificationId = "notif123";
        final var aToken = "tokenNotif123";
        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        final var body = Map.of(
                "code", 200,
                "data", List.of(
                        Map.of(
                                "id", 123L,
                                "type", "PAYMENT",
                                "custom_id", "custom123",
                                "status", Map.of("current", "PAID", "previous", "PENDING"),
                                "identifiers", Map.of("charge_id", 456L, "carnet_id", 789L),
                                "created_at", "2025-08-31T12:00:00Z"
                        )
                )
        );

        stubFor(get(urlPathEqualTo("/v1/notification/" + notificationId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(writeValueAsString(body))));

        final var notification = gateway.getNotifications(notificationId);

        Assertions.assertNotNull(notification);
        Assertions.assertEquals(200, notification.code());
        Assertions.assertEquals(1, notification.data().size());
        final var data = notification.data().get(0);
        Assertions.assertEquals(123L, data.id());
        Assertions.assertEquals("PAID", data.currentStatus());
        Assertions.assertEquals(456L, data.chargeId());

        verify(1, getRequestedFor(urlEqualTo("/v1/notification/" + notificationId))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + aToken)));
    }

    @Test
    void givenNotificationNotFound_whenGetNotifications_thenThrowNotFoundException() {
        final var notificationId = "notif404";
        final var aToken = "tokenNotif404";
        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(get(urlPathEqualTo("/v1/notification/" + notificationId))
                .willReturn(aResponse().withStatus(404)));

        final var exception = Assertions.assertThrows(NotFoundException.class,
                () -> gateway.getNotifications(notificationId));

        Assertions.assertTrue(exception.getMessage().contains(notificationId));
        verify(1, getRequestedFor(urlEqualTo("/v1/notification/" + notificationId)));
    }

    @Test
    void givenNotificationService5xx_whenGetNotifications_thenThrowInternalErrorException() {
        final var notificationId = "notif500";
        final var aToken = "tokenNotif500";
        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(get(urlPathEqualTo("/v1/notification/" + notificationId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        final var exception = Assertions.assertThrows(InternalErrorException.class,
                () -> gateway.getNotifications(notificationId));

        Assertions.assertTrue(exception.getMessage().contains("Error observed"));
        verify(1, getRequestedFor(urlEqualTo("/v1/notification/" + notificationId)));
    }

    @Test
    void givenNotificationService429_whenGetNotifications_thenThrowTooManyRequestsException() {
        final var notificationId = "notif429";
        final var aToken = "tokenNotif429";
        Mockito.doReturn(aToken).when(getChargesClientCredentials).retrieve();

        stubFor(get(urlPathEqualTo("/v1/notification/" + notificationId))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"nome\":\"TooManyRequests\",\"mensagem\":\"Rate limit exceeded\"}")));

        final var exception = Assertions.assertThrows(TooManyRequestsException.class,
                () -> gateway.getNotifications(notificationId));

        Assertions.assertEquals("TooManyRequestsException", exception.getMessage());
        verify(1, getRequestedFor(urlEqualTo("/v1/notification/" + notificationId)));
    }


    @Override
    public InMemorySpanExporter getSpanExporter() {
        return spanExporter;
    }
}