package com.kaua.events.platform.application.usecases.users.create;

import com.kaua.events.platform.domain.users.User;

public record CreateUserOutput(
        String userId
) {

    public static CreateUserOutput from(final User aUser) {
        return new CreateUserOutput(
                aUser.getId().value().toString()
        );
    }
}
