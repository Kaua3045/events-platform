package com.kaua.events.platform.application.usecases.users.retrive.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

class GetUserByIdUseCaseTest extends UseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultGetUserByIdUseCase useCase;

    @Test
    void givenAValidUserId_whenCallGetUserByIdUseCase_thenReturnUser() {
        final var aUser = Fixture.UserFixture.newUser();
        final var aId = aUser.getId().value().toString();

        final var aInput = GetUserByIdInput.with(aId);

        Mockito.when(userRepository.userOfId(aId))
                .thenReturn(Optional.of(aUser));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aId, aOutput.id());
        Assertions.assertEquals(aUser.getName().firstName(), aOutput.firstName());
        Assertions.assertEquals(aUser.getName().lastName(), aOutput.lastName());
        Assertions.assertEquals(aUser.getEmail().value(), aOutput.email());
        Assertions.assertEquals(aUser.getRole().name(), aOutput.role());
        Assertions.assertEquals(aUser.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aUser.getUpdatedAt(), aOutput.updatedAt());
        Assertions.assertEquals(aUser.getVersion(), aOutput.version());

        Mockito.verify(userRepository, Mockito.times(1)).userOfId(aId);
    }

    @Test
    void givenAnInvalidNullInput_whenCallGetUserByIdUseCase_thenThrowsUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to GetUserByIdUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.never()).userOfId(Mockito.any());
    }

    @Test
    void givenAnInvalidUserId_whenCallGetUserByIdUseCase_thenThrowsNotFoundException() {
        final var aId = "1234567890";

        final var expectedErrorMessage = "User with id 1234567890 was not found";

        final var aInput = GetUserByIdInput.with(aId);

        Mockito.when(userRepository.userOfId(aId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).userOfId(aId);
    }
}
