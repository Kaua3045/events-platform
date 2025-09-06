package com.kaua.events.platform.application.usecases.users.update.phone;

import com.kaua.events.platform.domain.users.User;

public record UpdateUserPhoneNumberOutput(String userId) {

    public static UpdateUserPhoneNumberOutput from(final User aUser) {
        return new UpdateUserPhoneNumberOutput(aUser.getId().value().toString());
    }
}
