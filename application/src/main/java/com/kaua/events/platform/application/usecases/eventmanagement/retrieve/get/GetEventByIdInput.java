package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get;

public record GetEventByIdInput(String eventId, String organizationId) {

    public static GetEventByIdInput with(final String eventId, final String organizationId) {
        return new GetEventByIdInput(eventId, organizationId);
    }
}
