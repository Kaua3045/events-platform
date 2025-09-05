package com.kaua.events.platform.infrastructure.users.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.update.phone.UpdateUserPhoneNumberInput;

public record UpdateUserPhoneNumberRequest(
        @JsonProperty("phone_number") String phoneNumber,
        @JsonProperty("default_region") String defaultRegion
) {

    public UpdateUserPhoneNumberInput toInput(
            final String aUserId
    ) {
        return new UpdateUserPhoneNumberInput(aUserId, phoneNumber(), defaultRegion());
    }
}
