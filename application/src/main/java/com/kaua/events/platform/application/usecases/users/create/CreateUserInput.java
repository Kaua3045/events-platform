package com.kaua.events.platform.application.usecases.users.create;

import com.kaua.events.platform.domain.validation.AssertionConcern;

public record CreateUserInput(
        String firstName,
        String lastName,
        String email,
        String password
) implements AssertionConcern {

    public CreateUserInput {
        this.assertArgumentNotEmpty(firstName, "firstName", "cannot be null or empty");
        this.assertArgumentNotEmpty(lastName, "lastName", "cannot be null or empty");
        this.assertArgumentNotEmpty(email, "email", "cannot be null or empty");
        this.assertArgumentNotEmpty(password, "password", "cannot be null or empty");
    }

    public static CreateUserInput with(
            final String firstName,
            final String lastName,
            final String email,
            final String password
    ) {
        return new CreateUserInput(firstName, lastName, email, password);
    }
}
