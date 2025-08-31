package com.kaua.events.platform.infrastructure.rest.webhooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.payments.process.charges.ProcessPaymentChargeUseCase;
import com.kaua.events.platform.infrastructure.rest.controllers.webhooks.PaymentWebhookController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(controllers = PaymentWebhookController.class)
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private ProcessPaymentChargeUseCase processPaymentChargeUseCase;

    @Test
    void givenAValidWebhook_whenReceivePixWebhook_thenReturnNoContent() throws Exception {
        final var headers = Map.of(
                "x-idempotency-key", "test-key",
                "x-some-header", "some-value"
        );

        final var payload = Map.of(
                "payment_id", "pix-123",
                "status", "PAID",
                "amount", 1000
        );

        var mvcRequest = MockMvcRequestBuilders.post("/webhooks/pix")
                .with(ApiTest.admin("admin-user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(new HttpHeaders(MultiValueMap.fromSingleValue(headers)))
                .content(mapper.writeValueAsString(payload));

        mvc.perform(mvcRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    void givenAValidWebhook_whenReceiveCardWebhook_thenReturnNoContent() throws Exception {
        final var headers = Map.of(
                "x-idempotency-key", "test-key",
                "x-some-header", "some-value"
        );

        var mvcRequest = MockMvcRequestBuilders.post("/webhooks/card/notification")
                .with(ApiTest.admin("admin-user"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .headers(new HttpHeaders(MultiValueMap.fromSingleValue(headers)))
                .param("notification", "card-123");

        mvc.perform(mvcRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }
}
