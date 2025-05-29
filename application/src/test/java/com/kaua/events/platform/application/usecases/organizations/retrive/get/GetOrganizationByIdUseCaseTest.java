package com.kaua.events.platform.application.usecases.organizations.retrive.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.DefaultGetOrganizationByIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdInput;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

class GetOrganizationByIdUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private DefaultGetOrganizationByIdUseCase useCase;

    @Test
    void givenAValidOrganizationId_whenCallGetOrganizationByIdUseCase_thenReturnOrganization() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aId = aOrganization.getId().value().toString();

        final var aInput = GetOrganizationByIdInput.with(aId);

        Mockito.when(organizationRepository.organizationOfId(aId))
                .thenReturn(Optional.of(aOrganization));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aId, aOutput.id());
        Assertions.assertEquals(aOrganization.getVersion(), aOutput.version());
        Assertions.assertEquals(aOrganization.getName(), aOutput.name());
        Assertions.assertEquals(aOrganization.getDescription().get(), aOutput.description());
        Assertions.assertEquals(aOrganization.isDeleted(), aOutput.isDeleted());
        Assertions.assertEquals(aOrganization.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aOrganization.getUpdatedAt(), aOutput.updatedAt());
        Assertions.assertEquals(aOrganization.getDeletedAt().orElse(null), aOutput.deletedAt());

        Mockito.verify(organizationRepository, Mockito.times(1)).organizationOfId(aId);
    }

    @Test
    void givenAnInvalidNullInput_whenCallGetOrganizationByIdUseCase_thenThrowsUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to GetOrganizationByIdUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.never()).organizationOfId(Mockito.any());
    }

    @Test
    void givenAnInvalidOrganizationId_whenCallGetOrganizationByIdUseCase_thenThrowsNotFoundException() {
        final var aId = "1234567890";

        final var expectedErrorMessage = "Organization with id 1234567890 was not found";

        final var aInput = GetOrganizationByIdInput.with(aId);

        Mockito.when(organizationRepository.organizationOfId(aId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).organizationOfId(aId);
    }
}
