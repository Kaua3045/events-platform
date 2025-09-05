package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdInput;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdUseCase;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentUseCase;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.rest.UserAPI;
import com.kaua.events.platform.infrastructure.users.req.CreateUserRequest;
import com.kaua.events.platform.infrastructure.users.req.UpdateUserDocumentRequest;
import com.kaua.events.platform.infrastructure.users.res.CreateUserResponse;
import com.kaua.events.platform.infrastructure.users.res.GetUserByIdResponse;
import com.kaua.events.platform.infrastructure.users.res.UpdateUserDocumentResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class UserRestController implements UserAPI {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserDocumentUseCase updateUserDocumentUseCase;

    public UserRestController(
            final CreateUserUseCase createUserUseCase,
            final GetUserByIdUseCase getUserByIdUseCase,
            final UpdateUserDocumentUseCase updateUserDocumentUseCase
    ) {
        this.createUserUseCase = Objects.requireNonNull(createUserUseCase);
        this.getUserByIdUseCase = Objects.requireNonNull(getUserByIdUseCase);
        this.updateUserDocumentUseCase = Objects.requireNonNull(updateUserDocumentUseCase);
    }

    @IdempotencyKey
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

    @Override
    public ResponseEntity<UpdateUserDocumentResponse> updateDocument(
            final AuthenticatedUser aUser,
            final UpdateUserDocumentRequest aRequest
    ) {
        final var aInput = aRequest.toInput(aUser.id());

        final var aOutput = this.updateUserDocumentUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UpdateUserDocumentResponse.from(aOutput));
    }
}
