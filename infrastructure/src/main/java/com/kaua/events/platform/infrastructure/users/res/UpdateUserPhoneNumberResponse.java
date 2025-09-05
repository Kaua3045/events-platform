package com.kaua.events.platform.infrastructure.users.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.update.phone.UpdateUserPhoneNumberOutput;

public record UpdateUserPhoneNumberResponse(
        @JsonProperty("id") String id
) {

    public static UpdateUserPhoneNumberResponse from(final UpdateUserPhoneNumberOutput aOutput) {
        return new UpdateUserPhoneNumberResponse(aOutput.userId());
    }
}
