package com.kaua.events.platform.application.usecases.auth.code.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.Password;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import com.kaua.events.platform.domain.users.User;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultCreateAuthorizationCodeUseCase extends CreateAuthorizationCodeUseCase {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncryption passwordEncryption;

    public DefaultCreateAuthorizationCodeUseCase(
            final AuthorizationCodeRepository authorizationCodeRepository,
            final UserRepository userRepository,
            final PasswordEncryption passwordEncryption
    ) {
        this.authorizationCodeRepository = Objects.requireNonNull(authorizationCodeRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncryption = Objects.requireNonNull(passwordEncryption);
    }

    @Override
    public CreateAuthorizationCodeOutput execute(final CreateAuthorizationCodeInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(CreateAuthorizationCodeUseCase.class);

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

        final var aActualAuthorizationCode = this.authorizationCodeRepository.save(aAuthorizationCode);

        return new CreateAuthorizationCodeOutput(
                aActualAuthorizationCode.getCode(),
                aActualAuthorizationCode.getRedirectUri().concat("?code=")
                        .concat(aActualAuthorizationCode.getCode())
        );
    }
}
