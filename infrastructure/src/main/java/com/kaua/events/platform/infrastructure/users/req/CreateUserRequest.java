package com.kaua.events.platform.infrastructure.users.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.create.CreateUserInput;

public record CreateUserRequest(
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password
) {

    public CreateUserInput toInput() {
        return new CreateUserInput(
                this.firstName(),
                this.lastName(),
                this.email(),
                this.password()
        );
    }
}
