package com.kaua.events.platform.infrastructure.eventmanagement.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.update.UpdateEventInput;

import java.time.Instant;

public record UpdateEventRequest(
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("address") UpdateEventAddressRequest address,
        @JsonProperty("category_id") String categoryId,
        @JsonProperty("start_at") Instant startAt,
        @JsonProperty("finish_at") Instant finishAt
) {

    public UpdateEventInput toInput(final String userId, final String eventId) {
        return new UpdateEventInput(
                userId,
                eventId,
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
