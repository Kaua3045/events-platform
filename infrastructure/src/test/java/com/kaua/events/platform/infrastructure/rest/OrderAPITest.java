package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutInput;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutOutput;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdOutput;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdUseCase;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.users.UserID;
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

import java.util.List;

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

    @MockitoBean
    private ListOrdersByUserIdUseCase listOrdersByUserIdUseCase;

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

    @Test
    void givenAValidValues_whenCallListOrders_thenReturnOrdersPaginated() throws Exception {
        final var aUserId = ULID.random();

        final var aOrderOne = Fixture.OrderFixture.newOrder(new UserID(aUserId), List.of(
                Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())
        ));
        final var aOrderTwo = Fixture.OrderFixture.newOrder(new UserID(aUserId), List.of(
                Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())
        ));

        final var aPage = 0;
        final var aPerPage = 2;
        final var aTerms = "";
        final var aSort = "createdAt";
        final var aDirection = "desc";
        final var aItemsCount = 2;
        final var aPagesCount = 1;

        final var aItems = List.of(ListOrdersByUserIdOutput.from(aOrderOne), ListOrdersByUserIdOutput.from(aOrderTwo));
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aPagesCount, aItemsCount);

        Mockito.when(listOrdersByUserIdUseCase.execute(any()))
                .thenReturn(new Pagination<>(aMetadata, aItems));

        var aRequest = MockMvcRequestBuilders.get("/v1/orders")
                .with(ApiTest.admin(ULID.random().toString()))
                .queryParam("filters.status", "CREATED")
                .queryParam("page", String.valueOf(aPage))
                .queryParam("perPage", String.valueOf(aPerPage))
                .queryParam("search", aTerms)
                .queryParam("sort", aSort)
                .queryParam("direction", aDirection)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.metadata.current_page").value(aPage))
                .andExpect(jsonPath("$.metadata.per_page").value(aPerPage))
                .andExpect(jsonPath("$.metadata.total_pages").value(aPagesCount))
                .andExpect(jsonPath("$.metadata.total_items").value(aItemsCount))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].order_id").value(aOrderOne.getId().value().toString()))
                .andExpect(jsonPath("$.items[0].user_id").value(aOrderOne.getUserId().value().toString()))
                .andExpect(jsonPath("$.items[0].status").value(aOrderOne.getStatus().name()))
                .andExpect(jsonPath("$.items[0].items[0].item_id").value(aOrderOne.getItems().getFirst().getId().toString()))
                .andExpect(jsonPath("$.items[0].created_at").value(aOrderOne.getCreatedAt().toString()))
                .andExpect(jsonPath("$.items[0].updated_at").value(aOrderOne.getUpdatedAt().toString()));

        Mockito.verify(listOrdersByUserIdUseCase, Mockito.times(1)).execute(any());
    }
}
