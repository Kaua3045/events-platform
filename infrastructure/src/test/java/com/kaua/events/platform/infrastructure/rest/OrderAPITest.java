package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutInput;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutOutput;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutUseCase;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = OrderAPI.class)
class OrderAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateCheckoutUseCase createCheckoutUseCase;

    @Captor
    private ArgumentCaptor<CreateCheckoutInput> createCheckoutInputCaptor;

    @Test
    void givenAValidRequest_whenCallCreateCheckout_thenReturnCreatedOrder() throws Exception {
        final var aUserId = "user-123";
        final var anOrderId = "order-456";
        final var paymentMethod = "PIX";
        final var qrCodeUrl = "http://qr-code-url";

        final var aEventId = ULID.random().toString();
        final var aTicketId = ULID.random().toString();
        final var aQuantity = 2;

        Mockito.when(createCheckoutUseCase.execute(any()))
                .thenReturn(new CreateCheckoutOutput(anOrderId, paymentMethod, qrCodeUrl));

        var json = """
                {
                    "items": [
                        {
                            "event_id": "%s",
                            "ticket_id": "%s",
                            "quantity": %d
                        }
                    ],
                    "payment_details": {
                        "method": "PIX"
                    }
                }
                """.formatted(aEventId, aTicketId, aQuantity);

        var mvcRequest = MockMvcRequestBuilders.post("/v1/orders")
                .with(ApiTest.admin(aUserId))
                .header("x-idempotency-key", ULID.random().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        var response = mvc.perform(mvcRequest);

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.order_id").value(anOrderId))
                .andExpect(jsonPath("$.payment_method").value(paymentMethod))
                .andExpect(jsonPath("$.qr_code_url").value(qrCodeUrl));

        Mockito.verify(createCheckoutUseCase, Mockito.times(1)).execute(createCheckoutInputCaptor.capture());
        var capturedInput = createCheckoutInputCaptor.getValue();

        Assertions.assertEquals(aUserId, capturedInput.userId());
        Assertions.assertEquals(1, capturedInput.items().size());
        Assertions.assertEquals(aEventId, capturedInput.items().getFirst().eventId());
        Assertions.assertEquals(aTicketId, capturedInput.items().getFirst().ticketId());
        Assertions.assertEquals(2, capturedInput.items().getFirst().quantity());
        Assertions.assertEquals(paymentMethod, capturedInput.paymentDetails().method().name());
    }
}
