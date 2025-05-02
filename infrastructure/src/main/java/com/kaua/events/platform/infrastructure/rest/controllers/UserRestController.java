package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdInput;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdUseCase;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.rest.UserAPI;
import com.kaua.events.platform.infrastructure.users.req.CreateUserRequest;
import com.kaua.events.platform.infrastructure.users.res.CreateUserResponse;
import com.kaua.events.platform.infrastructure.users.res.GetUserByIdResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class UserRestController implements UserAPI {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;

    public UserRestController(
            final CreateUserUseCase createUserUseCase,
            final GetUserByIdUseCase getUserByIdUseCase
    ) {
        this.createUserUseCase = Objects.requireNonNull(createUserUseCase);
        this.getUserByIdUseCase = Objects.requireNonNull(getUserByIdUseCase);
    }

    @Override
    public ResponseEntity<CreateUserResponse> createUser(final CreateUserRequest request) {
        // TODO - In the future, after create user, need call auth usecase to create user auth (jwt)
        final var aOutput = this.createUserUseCase.execute(request.toInput());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/v1/users/me")
                .body(CreateUserResponse.from(aOutput));
    }

    @Override
    public ResponseEntity<GetUserByIdResponse> getMe(final AuthenticatedUser user) {
        final var aInput = GetUserByIdInput.with(user.id());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GetUserByIdResponse.from(this.getUserByIdUseCase.execute(aInput)));
    }
}
