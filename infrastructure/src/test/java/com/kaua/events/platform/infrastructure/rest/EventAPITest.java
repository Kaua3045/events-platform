package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventInput;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventOutput;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
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
}
