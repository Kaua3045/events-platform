package com.kaua.events.platform.infrastructure.gateways;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.payments.CreditCardPaymentDetails;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryPaymentGatewayTest extends UnitTest {

    private WebClient webClient;
    private InMemoryPaymentGateway gateway;

    private WebClient.RequestBodyUriSpec uriSpec;
    private WebClient.RequestBodySpec bodySpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        bodySpec = mock(WebClient.RequestBodySpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        gateway = new InMemoryPaymentGateway(webClient);
    }

    @Test
    void givenValidPix_whenProcess_thenShouldReturnActivePayment() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        final var response = gateway.process(request);

        assertNotNull(response);
        assertEquals(PaymentGateway.PaymentProcessStatus.ACTIVE, response.status());
        assertTrue(response.qrCode().startsWith("FAKE-PIX-"));
        assertTrue(response.qrCodeImageUrl().contains("tx123"));
    }

    @Test
    void givenPixAmountOutOfRange_whenProcess_thenShouldReturnFailedPayment() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("0.5"))
        );

        final var response = gateway.process(request);

        assertNotNull(response);
        assertEquals(PaymentGateway.PaymentProcessStatus.FAILED, response.status());
    }

    @Test
    void givenDuplicateTransaction_whenProcess_thenShouldReturnSamePayment() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        final var first = gateway.process(request);
        final var second = gateway.process(request);

        assertSame(first, second);
    }

    @Test
    void givenNonPixPayment_whenProcess_thenShouldThrowException() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new CreditCardPaymentDetails(
                        new BigDecimal("10"),
                        "John Doe",
                        "123.456.789-00",
                        "john.doe@mail.com",
                        "120834182789",
                        1
                )
        );

        final var ex = assertThrows(UnsupportedOperationException.class, () -> gateway.process(request));
        assertEquals("Only PIX is supported in local in-memory gateway", ex.getMessage());
    }

    @Test
    void whenProcessingPix_thenWebhookShouldBeCalled() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        final var aGateway = new InMemoryPaymentGateway(webClient, Runnable::run);

        aGateway.process(request);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
            verify(bodySpec, atLeastOnce()).bodyValue(captor.capture());
            Map payload = captor.getValue();
            assertNotNull(payload.get("pix"));
        });
    }

    @Test
    void clearStoreShouldEmptyTheMap() {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        gateway.process(request);
        gateway.clearStore();

        final var newResponse = gateway.process(request);
        assertNotNull(newResponse);
    }

    @Test
    void givenWebClientError_whenWebhook_thenDoOnErrorIsCalled() {
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.error(new RuntimeException("Webhook failed")));

        var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        final var aGateway = new InMemoryPaymentGateway(webClient, Runnable::run);

        assertDoesNotThrow(() -> aGateway.process(request));

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(bodySpec, atLeastOnce()).bodyValue(captor.capture());
        Map payload = captor.getValue();
        assertNotNull(payload.get("pix"));
        System.out.println(responseSpec);
    }

    @Test
    void whenWebhookSleepInterrupted_thenShouldLogError() throws InterruptedException {
        final var request = new PaymentGateway.PaymentProcessRequest(
                "tx123",
                "order123",
                new PixPaymentDetails(new BigDecimal("10"))
        );

        var gatewaySpy = spy(new InMemoryPaymentGateway(webClient, Runnable::run));
        doThrow(new InterruptedException("Simulated interruption")).when(gatewaySpy).sleep(anyLong());

        assertDoesNotThrow(() -> gatewaySpy.process(request));
    }

    @Test
    void givenPixAmountAtMinimum_whenProcess_thenShouldReturnActivePayment() {
        var request = new PaymentGateway.PaymentProcessRequest(
                "tx-min",
                "order-min",
                new PixPaymentDetails(new BigDecimal("1"))
        );

        var response = gateway.process(request);

        assertEquals(PaymentGateway.PaymentProcessStatus.ACTIVE, response.status());
    }

    @Test
    void givenPixAmountAtMaximum_whenProcess_thenShouldReturnActivePayment() {
        var request = new PaymentGateway.PaymentProcessRequest(
                "tx-max",
                "order-max",
                new PixPaymentDetails(new BigDecimal("50"))
        );

        var response = gateway.process(request);

        assertEquals(PaymentGateway.PaymentProcessStatus.ACTIVE, response.status());
    }

    @Test
    void givenPixAmountAboveMaximum_whenProcess_thenShouldReturnFailedPayment() {
        var request = new PaymentGateway.PaymentProcessRequest(
                "tx-above",
                "order-above",
                new PixPaymentDetails(new BigDecimal("51"))
        );

        var response = gateway.process(request);

        assertEquals(PaymentGateway.PaymentProcessStatus.FAILED, response.status());
    }
}
