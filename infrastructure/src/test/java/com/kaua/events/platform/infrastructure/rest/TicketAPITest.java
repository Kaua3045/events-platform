package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketInput;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketOutput;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.list.ListTicketsOutput;
import com.kaua.events.platform.application.usecases.ticket.retrieve.list.ListTicketsUseCase;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = TicketAPI.class)
class TicketAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateTicketUseCase createTicketUseCase;

    @MockitoBean
    private ListTicketsUseCase listTicketsUseCase;

    @Captor
    private ArgumentCaptor<CreateTicketInput> createTicketInputCaptor;

    @Test
    void givenAValidRequest_whenCallCreateTicket_thenReturnTicketIdAndEventId() throws Exception {
        final var aUserId = ULID.random().toString();
        final var anEventId = ULID.random().toString();
        final var aName = "An Event Name";
        final var aDescription = "An Event Description";
        final var aPrice = BigDecimal.TEN;
        final var aQuantity = 100;
        final var aType = "free";
        final var aStatus = "active";

        final var aExpectedTicketId = ULID.random().toString();

        Mockito.when(createTicketUseCase.execute(any()))
                .thenAnswer(call -> new CreateTicketOutput(aExpectedTicketId, anEventId));

        var json = """
                {
                    "event_id": "%s",
                    "name": "%s",
                    "description": "%s",
                    "price": %s,
                    "quantity": %d,
                    "type": "%s",
                    "status": "%s"
                }
                """.formatted(
                anEventId,
                aName,
                aDescription,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        final var aRequest = MockMvcRequestBuilders.post("/v1/tickets")
                .with(ApiTest.admin(aUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.ticket_id").value(aExpectedTicketId))
                .andExpect(jsonPath("$.event_id").value(anEventId));

        Mockito.verify(createTicketUseCase, Mockito.times(1)).execute(createTicketInputCaptor.capture());

        final var aCreateTicketInput = createTicketInputCaptor.getValue();

        Assertions.assertEquals(aUserId, aCreateTicketInput.userId());
        Assertions.assertEquals(anEventId, aCreateTicketInput.eventId());
        Assertions.assertEquals(aType, aCreateTicketInput.type());
        Assertions.assertEquals(aStatus, aCreateTicketInput.status());
        Assertions.assertEquals(aPrice.toString(), aCreateTicketInput.price());
    }

    @Test
    void givenAValidValues_whenCallListTickets_thenReturnTicketsPaginated() throws Exception {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.newTicket(aEventId);
        final var aTicketTwo = Fixture.TicketFixture.newTicket(aEventId);

        final var aPage = 0;
        final var aPerPage = 2;
        final var aTerms = "";
        final var aSort = "created_at";
        final var aDirection = "asc";
        final var aItemsCount = 2;
        final var aPagesCount = 1;

        final var aItems = List.of(ListTicketsOutput.from(aTicketOne), ListTicketsOutput.from(aTicketTwo));
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aPagesCount, aItemsCount);

        Mockito.when(listTicketsUseCase.execute(any()))
                .thenReturn(new Pagination<>(aMetadata, aItems));

        var aRequest = MockMvcRequestBuilders.get("/v1/tickets")
                .with(ApiTest.admin(ULID.random().toString()))
                .queryParam("filters.eventId", aEventId.value().toString())
                .queryParam("filters.status", "AVAILABLE")
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
                .andExpect(jsonPath("$.items[0].ticket_id").value(aTicketOne.getId().value().toString()))
                .andExpect(jsonPath("$.items[0].event_id").value(aTicketOne.getEventId().value().toString()))
                .andExpect(jsonPath("$.items[0].name").value(aTicketOne.getName()))
                .andExpect(jsonPath("$.items[0].description").value(aTicketOne.getDescription().orElse(null)))
                .andExpect(jsonPath("$.items[0].quantity").value(aTicketOne.getQuantity()))
                .andExpect(jsonPath("$.items[0].sold").value(aTicketOne.getSold()))
                .andExpect(jsonPath("$.items[0].type").value(aTicketOne.getType().name()))
                .andExpect(jsonPath("$.items[0].status").value(aTicketOne.getStatus().name()))
                .andExpect(jsonPath("$.items[0].created_at").value(aTicketOne.getCreatedAt().toString()))
                .andExpect(jsonPath("$.items[0].updated_at").value(aTicketOne.getUpdatedAt().toString()));

        Mockito.verify(listTicketsUseCase, Mockito.times(1)).execute(any());
    }
}
