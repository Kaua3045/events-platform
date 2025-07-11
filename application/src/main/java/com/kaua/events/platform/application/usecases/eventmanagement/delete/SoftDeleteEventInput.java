package com.kaua.events.platform.application.usecases.eventmanagement.delete;

public record SoftDeleteEventInput(
        String eventId,
        String userId
) {

    public static SoftDeleteEventInput with(final String eventId, final String userId) {
        return new SoftDeleteEventInput(eventId, userId);
    }
}
