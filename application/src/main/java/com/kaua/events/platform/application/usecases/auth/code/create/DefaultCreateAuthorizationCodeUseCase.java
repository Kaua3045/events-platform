package com.kaua.events.platform.application.usecases.auth.code.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import com.kaua.events.platform.domain.users.User;

import java.util.Objects;

public class DefaultCreateAuthorizationCodeUseCase extends CreateAuthorizationCodeUseCase {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncryption passwordEncryption;
    private final TracerWrapper tracerWrapper;

    public DefaultCreateAuthorizationCodeUseCase(
            final AuthorizationCodeRepository authorizationCodeRepository,
            final UserRepository userRepository,
            final PasswordEncryption passwordEncryption,
            final TracerWrapper tracerWrapper
    ) {
        this.authorizationCodeRepository = Objects.requireNonNull(authorizationCodeRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncryption = Objects.requireNonNull(passwordEncryption);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreateAuthorizationCodeOutput execute(final CreateAuthorizationCodeInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createAuthorizationCodeUseCase",
                span -> {
                    if (input == null)
                        throw new UseCaseInputCannotBeNullException(CreateAuthorizationCodeUseCase.class);

                    final var aUser = this.userRepository.userOfEmail(input.email())
                            .orElseThrow(NotFoundException.with(User.class, "email", input.email()));

                    if (!this.passwordEncryption.matches(input.password(), aUser.getPassword().value())) {
                        throw NotFoundException.with("Email or password not found");
                    }

                    final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                            input.clientId(),
                            aUser.getId(),
                            input.redirectUri(),
                            input.codeChallenge(),
                            input.codeChallengeMethod()
                    );

                    span.setAttribute("userId", aUser.getId().value().toString());
                    span.setAttribute("oauth.clientId", input.clientId());
                    span.setAttribute("oauth.codeChallengeMethod", input.codeChallengeMethod());
                    span.setAttribute("oauth.redirectUri", input.redirectUri());

                    final var aActualAuthorizationCode = span.runInSpan(
                            "saveAuthorizationCode",
                            () -> this.authorizationCodeRepository.save(aAuthorizationCode)
                    );

                    return new CreateAuthorizationCodeOutput(
                            aActualAuthorizationCode.getCode(),
                            aActualAuthorizationCode.getRedirectUri().concat("?code=")
                                    .concat(aActualAuthorizationCode.getCode())
                    );
                }
        );
    }
}
