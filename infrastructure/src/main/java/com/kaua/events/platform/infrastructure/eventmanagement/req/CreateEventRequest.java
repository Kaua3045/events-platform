package com.kaua.events.platform.infrastructure.eventmanagement.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventInput;

import java.time.Instant;

public record CreateEventRequest(
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("address") CreateEventAddressRequest address,
        @JsonProperty("category_id") String categoryId,
        @JsonProperty("start_at") Instant startAt,
        @JsonProperty("finish_at") Instant finishAt
) {

    public CreateEventInput toInput() {
        return new CreateEventInput(
                organizationId,
                title,
                description,
                eventType,
                address == null ? null : address.toInput(),
                categoryId,
                startAt,
                finishAt
        );
    }
}
