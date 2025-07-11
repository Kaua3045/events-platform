package com.kaua.events.platform.application.usecases.organizations.retrive.members.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.DefaultGetOrganizationMemberByUserIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdInput;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

class GetOrganizationMemberByUserIdUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultGetOrganizationMemberByUserIdUseCase useCase;

    @Test
    void givenAValidUserId_whenCallGetOrganizationMemberByUserIdUseCase_thenReturnOrganizationMember() {
        final var aMember = Fixture.OrganizationMemberFixture
                .newOwnerMember(new OrganizationID(ULID.random()), new UserID(ULID.random()));
        final var aUserId = aMember.getUserId().value().toString();

        final var aInput = GetOrganizationMemberByUserIdInput.with(aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aUserId, aOutput.userId());
        Assertions.assertEquals(aMember.getOrganizationId().value().toString(), aOutput.organizationId());
        Assertions.assertEquals(aMember.getMemberRole().name(), aOutput.memberRole());
        Assertions.assertEquals(aMember.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aMember.getUpdatedAt(), aOutput.updatedAt());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
    }

    @Test
    void givenAnInvalidNullInput_whenCallGetOrganizationMemberByUserIdUseCase_thenThrowsUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to GetOrganizationMemberByUserIdUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
    }

    @Test
    void givenAnInvalidUserId_whenCallGetOrganizationMemberByUserIdUseCase_thenThrowsNotFoundException() {
        final var aUserId = "1234567890";

        final var expectedErrorMessage = "OrganizationMember with id 1234567890 was not found";

        final var aInput = GetOrganizationMemberByUserIdInput.with(aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
    }
}
