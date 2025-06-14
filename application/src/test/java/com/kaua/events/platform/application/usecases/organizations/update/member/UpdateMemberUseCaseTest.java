package com.kaua.events.platform.application.usecases.organizations.update.member;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class UpdateMemberUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultUpdateMemberUseCase useCase;

    @Test
    void givenAValidValues_whenCallUpdateMemberUseCase_thenReturnMemberId() {
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                aOrganizationId,
                new UserID(ULID.random())
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                aOrganizationId,
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));
        Mockito.when(organizationMemberRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aMemberToUpdate.getUserId().value().toString(), aOutput.userId());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .save(argThat(aCmd ->
                        Objects.equals(aMemberToUpdate.getOrganizationId(), aCmd.getOrganizationId())
                                && Objects.equals(aMemberToUpdate.getUserId(), aCmd.getUserId())
                                && Objects.equals(aRoleToUpdate, aCmd.getMemberRole())));
    }

    @Test
    void givenAnInvalidAuthenticatedMember_whenCallUpdateMemberUseCase_thenThrowsNotFoundException() {
        final var aAuthenticatedUserId = new UserID(ULID.random());
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedUserId.value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedUserId.value().toString()))
                .thenReturn(Optional.empty());

        final var expectedErrorMessage = "OrganizationMember with id %s was not found".formatted(aAuthenticatedUserId.value());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedUserId.value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnInvalidMemberToUpdate_whenCallUpdateMemberUseCase_thenThrowsNotFoundException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );
        final var aMemberToUpdateId = new UserID(ULID.random());
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdateId.value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdateId.value().toString()))
                .thenReturn(Optional.empty());

        final var expectedErrorMessage = "OrganizationMember with id %s was not found".formatted(aMemberToUpdateId.value());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdateId.value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnInvalidMemberRole_whenCallUpdateMemberUseCase_thenThrowsNotFoundException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                aAuthenticatedMember.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = "INVALID_ROLE";

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));

        final var expectedErrorMessage = "Member role with name %s not found.".formatted(aRoleToUpdate);

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnAuthenticatedMemberWithMemberRole_whenCallUpdateMemberUseCase_thenThrowsDomainException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                aAuthenticatedMember.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));

        final var expectedErrorMessage = "You cannot update a member with MEMBER role.";

        final var aException = Assertions.assertThrows(com.kaua.events.platform.domain.exceptions.DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnAuthenticatedMemberFromAnotherOrganization_whenCallUpdateMemberUseCase_thenThrowsDomainException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));

        final var expectedErrorMessage = "You cannot update a member that is not part of your organization.";

        final var aException = Assertions.assertThrows(com.kaua.events.platform.domain.exceptions.DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnInvalidOwnerRole_whenCallUpdateMemberUseCase_thenThrowsDomainException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                aAuthenticatedMember.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.OWNER;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));

        final var expectedErrorMessage = "You cannot update a member to OWNER role.";

        final var aException = Assertions.assertThrows(com.kaua.events.platform.domain.exceptions.DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnInvalidAuthenticatedMemberRoleIsAdmin_whenCallUpdateMemberUseCase_thenThrowsDomainException() {
        final var aAuthenticatedMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.ADMIN
        );
        final var aMemberToUpdate = Fixture.OrganizationMemberFixture.newMember(
                aAuthenticatedMember.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aRoleToUpdate = OrganizationMemberRole.ADMIN;

        final var aInput = UpdateMemberInput.with(
                aAuthenticatedMember.getUserId().value().toString(),
                aMemberToUpdate.getUserId().value().toString(),
                aRoleToUpdate.name()
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(aAuthenticatedMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aAuthenticatedMember));
        Mockito.when(organizationMemberRepository.memberOfUserId(aMemberToUpdate.getUserId().value().toString()))
                .thenReturn(Optional.of(aMemberToUpdate));

        final var expectedErrorMessage = "Only the owner can update a member to a different role.";

        final var aException = Assertions.assertThrows(com.kaua.events.platform.domain.exceptions.DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aAuthenticatedMember.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(aMemberToUpdate.getUserId().value().toString());
        Mockito.verify(organizationMemberRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAnInvalidNullInput_whenCallUpdateMemberUseCase_thenThrowsUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to UpdateMemberUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.never()).save(Mockito.any());
    }
}
