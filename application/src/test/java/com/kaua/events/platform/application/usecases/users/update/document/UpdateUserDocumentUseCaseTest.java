package com.kaua.events.platform.application.usecases.users.update.document;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.*;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;

class UpdateUserDocumentUseCaseTest extends UseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultUpdateUserDocumentUseCase useCase;

    @Test
    void givenAValidInput_whenCallUpdateUserDocumentUseCase_thenReturnOutput() {
        final var aUserId = IdentifierUtils.generateNewULID().toString();
        final var aDocumentNumber = "217.641.740-20";
        final var aDocumentType = "cpf";

        final var aUser = User.newUser(
                new Name("john", "doe"),
                new Email("john.doe@mail.com"),
                Password.of("123456Am*"),
                UserRole.USER
        );

        final var aInput = UpdateUserDocumentInput.with(aUserId, aDocumentNumber, aDocumentType);

        Mockito.when(userRepository.userOfId(aUserId)).thenReturn(Optional.of(aUser));
        Mockito.when(userRepository.save(any())).thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aUser.getId().value().toString(), aOutput.userId());

        Mockito.verify(userRepository, Mockito.times(1)).userOfId(aUserId);
        Mockito.verify(userRepository, Mockito.times(1)).save(Objects.requireNonNull(aUser));
    }

    @Test
    void givenANonExistingUser_whenCallUpdateUserDocumentUseCase_thenThrowNotFoundException() {
        final var aUserId = IdentifierUtils.generateNewULID().toString();
        final var aDocumentNumber = "217.641.740-20";
        final var aDocumentType = "cpf";

        final var expectedErrorMessage = "User with id %s was not found".formatted(aUserId);

        final var aInput = UpdateUserDocumentInput.with(aUserId, aDocumentNumber, aDocumentType);

        Mockito.when(userRepository.userOfId(aUserId)).thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).userOfId(aUserId);
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void givenANullInput_whenCallUpdateUserDocumentUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedMessage = "Input to UpdateUserDocumentUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.never()).userOfId(any());
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }
}