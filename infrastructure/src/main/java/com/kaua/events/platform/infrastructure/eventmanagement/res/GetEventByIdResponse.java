package com.kaua.events.platform.infrastructure.eventmanagement.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get.GetEventByIdOutput;

import java.time.Instant;

public record GetEventByIdResponse(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("status") String status,
        @JsonProperty("type") String type,
        @JsonProperty("address") GetEventByIdAddressResponse address,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("category_id") String categoryId,
        @JsonProperty("start_at") Instant startAt,
        @JsonProperty("finish_at") Instant finishAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt
) {

    public static GetEventByIdResponse from(final GetEventByIdOutput aEvent) {
        return new GetEventByIdResponse(
                aEvent.eventId(),
                aEvent.organizationId(),
                aEvent.title(),
                aEvent.description(),
                aEvent.status(),
                aEvent.type(),
                aEvent.address() == null ? null : GetEventByIdAddressResponse.from(aEvent.address()),
                aEvent.imageUrl(),
                aEvent.categoryId(),
                aEvent.startAt(),
                aEvent.finishAt(),
                aEvent.createdAt(),
                aEvent.updatedAt(),
                aEvent.deletedAt()
        );
    }
}
