package com.kaua.events.platform.infrastructure.eventmanagement.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsOutput;

import java.time.Instant;

public record ListEventsResponse(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("category_id") String categoryId,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("event_status") String eventStatus,
        @JsonProperty("address_city") String addressCity,
        @JsonProperty("start_at") Instant startAt,
        @JsonProperty("finish_at") Instant finishAt
) {

    public static ListEventsResponse from(final ListEventsOutput aOutput) {
        return new ListEventsResponse(
                aOutput.eventId(),
                aOutput.organizationId(),
                aOutput.title(),
                aOutput.description(),
                aOutput.imageUrl(),
                aOutput.categoryId(),
                aOutput.eventType(),
                aOutput.eventStatus(),
                aOutput.addressCity(),
                aOutput.startAt(),
                aOutput.finishAt()
        );
    }
}
