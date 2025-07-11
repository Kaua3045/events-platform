package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventInput;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventOutput;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.delete.SoftDeleteEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsOutput;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsUseCase;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.eventmanagement.req.CreateEventAddressRequest;
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

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = EventAPI.class)
class EventAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateEventUseCase createEventUseCase;

    @MockitoBean
    private ListEventsUseCase listEventsUseCase;

    @MockitoBean
    private SoftDeleteEventUseCase softDeleteEventUseCase;

    @Captor
    private ArgumentCaptor<CreateEventInput> createEventInputCaptor;

    @Test
    void givenAValidRequestUsingRemoteEventType_whenCallCreateEvent_thenReturnOrganizationIdAndEventId() throws Exception {
        final var aOrganizationId = ULID.random();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aExpectedEventId = ULID.random().toString();

        Mockito.when(createEventUseCase.execute(any()))
                .thenAnswer(call -> new CreateEventOutput(aOrganizationId.toString(), aExpectedEventId));

        var json = """
                {
                    "title": "%s",
                    "description": "%s",
                    "event_type": "%s",
                    "category_id": "%s",
                    "start_at": "%s",
                    "finish_at": "%s"
                }
                """.formatted(aTitle, aDescription, aType, aCategoryId, aStartAt, aFinishAt);

        final var aRequest = MockMvcRequestBuilders.post("/v1/events")
                .with(ApiTest.admin())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.event_id").value(aExpectedEventId))
                .andExpect(jsonPath("$.organization_id").value(aOrganizationId.toString()));

        Mockito.verify(createEventUseCase, Mockito.times(1)).execute(createEventInputCaptor.capture());

        final var aCreateEventInput = createEventInputCaptor.getValue();

        Assertions.assertEquals(aTitle, aCreateEventInput.title());
        Assertions.assertEquals(aDescription, aCreateEventInput.description());
        Assertions.assertEquals(aType, aCreateEventInput.eventType());
        Assertions.assertEquals(aCategoryId, aCreateEventInput.categoryId());
        Assertions.assertEquals(aStartAt, aCreateEventInput.startAt());
        Assertions.assertEquals(aFinishAt, aCreateEventInput.finishAt());
    }

    @Test
    void givenAValidRequestUsingInPersonEventType_whenCallCreateEvent_thenReturnOrganizationIdAndEventId() throws Exception {
        final var aOrganizationId = ULID.random();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aStreet = "event-street";
        final var aNumber = "12345B";
        final var aComplement = "home";
        final var aNeighborhood = "baiiro";
        final var aCity = "city-test";
        final var aState = "state-test";
        final var aPostalCode = "120292831288";
        final var aCountry = "br-tes";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aExpectedEventId = ULID.random().toString();

        Mockito.when(createEventUseCase.execute(any()))
                .thenAnswer(call -> new CreateEventOutput(aOrganizationId.toString(), aExpectedEventId));

        final var aAddressRequest = new CreateEventAddressRequest(
                aStreet,
                aNumber,
                aComplement,
                aNeighborhood,
                aCity,
                aState,
                aPostalCode,
                aCountry
        );

        var json = """
                {
                    "title": "%s",
                    "description": "%s",
                    "event_type": "%s",
                    "category_id": "%s",
                    "start_at": "%s",
                    "finish_at": "%s",
                    "address": {
                        "street": "%s",
                        "number": "%s",
                        "complement": "%s",
                        "neighborhood": "%s",
                        "city": "%s",
                        "state": "%s",
                        "postal_code": "%s",
                        "country": "%s"
                    }
                }
                """.formatted(
                aTitle, aDescription, aType, aCategoryId, aStartAt, aFinishAt,
                aStreet, aNumber, aComplement, aNeighborhood, aCity, aState, aPostalCode, aCountry
        );

        final var aRequest = MockMvcRequestBuilders.post("/v1/events")
                .with(ApiTest.admin())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.event_id").value(aExpectedEventId))
                .andExpect(jsonPath("$.organization_id").value(aOrganizationId.toString()));

        Mockito.verify(createEventUseCase, Mockito.times(1)).execute(createEventInputCaptor.capture());

        final var aCreateEventInput = createEventInputCaptor.getValue();

        Assertions.assertEquals(aTitle, aCreateEventInput.title());
        Assertions.assertEquals(aDescription, aCreateEventInput.description());
        Assertions.assertEquals(aType, aCreateEventInput.eventType());
        Assertions.assertEquals(aCategoryId, aCreateEventInput.categoryId());
        Assertions.assertEquals(aStartAt, aCreateEventInput.startAt());
        Assertions.assertEquals(aFinishAt, aCreateEventInput.finishAt());
    }

    // TODO da forma que esta, funciona, mas o filters recebe todos os outros parametros, temos 2 opcoes
    // Manter assim, ou entao de alguma forma receber so o filters e remover os paramentros nao filter
    // E deixar no map so os filtros
    @Test
    void givenAValidValues_whenCallListEvents_thenReturnEventsPaginated() throws Exception {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent("aaaaa", aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent("bbbbbbb", aOrganizationId, ULID.random().toString());

        final var aPage = 0;
        final var aPerPage = 2;
        final var aTerms = "";
        final var aSort = "created_at";
        final var aDirection = "asc";
        final var aItemsCount = 2;
        final var aPagesCount = 1;

        final var aItems = List.of(ListEventsOutput.from(aEventOne), ListEventsOutput.from(aEventTwo));
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aPagesCount, aItemsCount);

        Mockito.when(listEventsUseCase.execute(any()))
                .thenReturn(new Pagination<>(aMetadata, aItems));

        var aRequest = MockMvcRequestBuilders.get("/v1/events")
                .with(ApiTest.admin(ULID.random().toString()))
                .queryParam("filters.categoryId", aEventOne.getCategoryId())
                .queryParam("filters.status", "active")
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
                .andExpect(jsonPath("$.items[0].event_id").value(aEventOne.getId().value().toString()))
                .andExpect(jsonPath("$.items[0].organization_id").value(aEventOne.getOrganizationId().value().toString()))
                .andExpect(jsonPath("$.items[0].title").value(aEventOne.getTitle()));

        Mockito.verify(listEventsUseCase, Mockito.times(1)).execute(any());
    }

    @Test
    void givenAValidEventIdAndAuthenticatedUser_whenCallSoftDeleteEvent_thenReturnNoContent() throws Exception {
        final var aEventId = ULID.random().toString();
        final var aUserId = ULID.random().toString();

        Mockito.doNothing().when(softDeleteEventUseCase).execute(any());

        final var aRequest = MockMvcRequestBuilders.delete("/v1/events/{eventId}", aEventId)
                .with(ApiTest.admin(aUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        Mockito.verify(softDeleteEventUseCase, Mockito.times(1)).execute(any());
    }
}
