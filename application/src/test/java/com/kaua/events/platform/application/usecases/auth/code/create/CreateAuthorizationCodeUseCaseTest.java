package com.kaua.events.platform.application.usecases.auth.code.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreateAuthorizationCodeUseCaseTest extends UseCaseTest {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncryption passwordEncryption;

    @InjectMocks
    private DefaultCreateAuthorizationCodeUseCase useCase;

    @Test
    void givenAValidValues_whenCallCreateAuthorizationCodeUseCase_thenReturnCodeAndRedirectUri() {
        final var aClientId = "clientId";
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "1234567Am*";

        final var aInput = new CreateAuthorizationCodeInput(
                aClientId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod,
                aEmail,
                aPassword
        );

        Mockito.when(userRepository.userOfEmail(aEmail))
                .thenReturn(Optional.of(User.newUser(
                        new Name("John", "Doe"),
                        new Email(aEmail),
                        Password.of(aPassword),
                        UserRole.USER
                )));
        Mockito.when(passwordEncryption.matches(aPassword, aPassword))
                .thenReturn(true);
        Mockito.when(authorizationCodeRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.code());
        Assertions.assertNotNull(aOutput.redirectUri());

        Mockito.verify(userRepository, Mockito.times(1)).userOfEmail(aEmail);
        Mockito.verify(passwordEncryption, Mockito.times(1)).matches(aPassword, aPassword);
        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.equals(aClientId, aCmd.getClientId())
                        && Objects.equals(aRedirectUri, aCmd.getRedirectUri())
                        && Objects.equals(aCodeChallenge, aCmd.getCodeChallenge())
                        && Objects.equals(aCodeChallengeMethod, aCmd.getCodeChallengeMethod())
                        && Objects.nonNull(aCmd.getUserId())
                        && Objects.nonNull(aCmd.getCreatedAt())
                        && Objects.nonNull(aCmd.getUpdatedAt())));
    }

    @Test
    void givenAnInvalidNullInput_whenCallCreateAuthorizationCodeUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateAuthorizationCodeUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.never()).userOfEmail(Mockito.any());
        Mockito.verify(passwordEncryption, Mockito.never()).matches(Mockito.any(), Mockito.any());
        Mockito.verify(authorizationCodeRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidPassword_whenCallCreateAuthorizationCodeUseCase_thenThrowNotFoundException() {
        final var aClientId = "clientId";
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "1234567Am*";

        final var expectedErrorMessage = "Email or password not found";

        final var aInput = new CreateAuthorizationCodeInput(
                aClientId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod,
                aEmail,
                aPassword
        );

        Mockito.when(userRepository.userOfEmail(aEmail))
                .thenReturn(Optional.of(User.newUser(
                        new Name("John", "Doe"),
                        new Email(aEmail),
                        Password.of(aPassword),
                        UserRole.USER
                )));
        Mockito.when(passwordEncryption.matches(aPassword, aPassword))
                .thenReturn(false);

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).userOfEmail(aEmail);
        Mockito.verify(passwordEncryption, Mockito.times(1)).matches(aPassword, aPassword);
        Mockito.verify(authorizationCodeRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEmail_whenCallCreateAuthorizationCodeUseCase_thenThrowNotFoundException() {
        final var aClientId = "clientId";
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "1234567Am*";

        final var expectedErrorMessage = "User with email john.doe@teste.com was not found";

        final var aInput = new CreateAuthorizationCodeInput(
                aClientId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod,
                aEmail,
                aPassword
        );

        Mockito.when(userRepository.userOfEmail(aEmail))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).userOfEmail(aEmail);
        Mockito.verify(passwordEncryption, Mockito.never()).matches(Mockito.any(), Mockito.any());
        Mockito.verify(authorizationCodeRepository, Mockito.never()).save(Mockito.any());
    }
}
