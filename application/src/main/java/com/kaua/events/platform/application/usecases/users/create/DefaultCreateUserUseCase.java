package com.kaua.events.platform.application.usecases.users.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.users.*;

import java.util.Objects;

public class DefaultCreateUserUseCase extends CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncryption passwordEncryption;
    private final TracerWrapper tracerWrapper;

    public DefaultCreateUserUseCase(
            final UserRepository userRepository,
            final PasswordEncryption passwordEncryption,
            final TracerWrapper tracerWrapper
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncryption = Objects.requireNonNull(passwordEncryption);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreateUserOutput execute(final CreateUserInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createUserUseCase",
                span -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreateUserUseCase.class);

                    span.runInSpan(
                            "checkIfExistsUserWithThisEmail",
                            () -> {
                                if (this.userRepository.existsByEmail(input.email())) {
                                    throw DomainException.with("Email already exists");
                                }
                            }
                    );

                    final var aName = new Name(input.firstName(), input.lastName());
                    final var aEmail = new Email(input.email());
                    final var aPassword = Password.create(input.password(), passwordEncryption);

                    final var aUser = User.newUser(
                            aName,
                            aEmail,
                            aPassword,
                            UserRole.USER
                    );

                    span.setAttribute("userId", aUser.getId().value().toString());

                    final var aUserCreated = span.runInSpan(
                            "saveUser",
                            () -> this.userRepository.save(aUser)
                    );
                    return CreateUserOutput.from(aUserCreated);
                }
        );
    }
}
