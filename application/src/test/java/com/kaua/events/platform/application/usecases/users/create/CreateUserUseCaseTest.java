package com.kaua.events.platform.application.usecases.users.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import com.kaua.events.platform.domain.users.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreateUserUseCaseTest extends UseCaseTest {

    @Mock
    private PasswordEncryption passwordEncryption;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultCreateUserUseCase useCase;

    @Test
    void givenAValidValues_whenCallCreateUserUseCase_thenReturnUserId() {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@test.com";
        final var aPassword = "123456Am@";

        final var aInput = CreateUserInput.with(aFirstName, aLastName, aEmail, aPassword);

        Mockito.when(userRepository.existsByEmail(aEmail))
                .thenReturn(false);
        Mockito.when(passwordEncryption.encrypt(aPassword))
                .thenReturn(aPassword);
        Mockito.when(userRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.userId());

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(aEmail);
        Mockito.verify(passwordEncryption, Mockito.times(1)).encrypt(aPassword);
        Mockito.verify(userRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.nonNull(aCmd.getId())
                        && Objects.equals(0L, aCmd.getVersion())
                        && Objects.equals(aFirstName, aCmd.getName().firstName())
                        && Objects.equals(aLastName, aCmd.getName().lastName())
                        && Objects.equals(aEmail, aCmd.getEmail().value())
                        && Objects.equals(aPassword, aCmd.getPassword().value())
                        && Objects.equals(aCmd.getRole(), UserRole.USER)
                        && Objects.nonNull(aCmd.getCreatedAt())
                        && Objects.nonNull(aCmd.getUpdatedAt())));
    }

    @Test
    void givenAnInvalidExistsEmail_whenCallCreateUserUseCase_thenThrowDomainException() {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@test.com";
        final var aPassword = "123456Am@";

        final var aExpectedErrorMessage = "Email already exists";

        final var aInput = CreateUserInput.with(aFirstName, aLastName, aEmail, aPassword);

        Mockito.when(userRepository.existsByEmail(aEmail))
                .thenReturn(true);

        final var aException = Assertions.assertThrows(DomainException.class, () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(aEmail);
        Mockito.verify(passwordEncryption, Mockito.never()).encrypt(aPassword);
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidInput_whenCallCreateUserUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateUserUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.never()).existsByEmail(Mockito.any());
        Mockito.verify(passwordEncryption, Mockito.never()).encrypt(Mockito.any());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }
}
