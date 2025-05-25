package com.kaua.events.platform.application.usecases.organizations.addMember;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
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

class AddMemberToOrganizationUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultAddMemberToOrganizationUseCase useCase;

    @Test
    void givenAValidValues_whenCallAddMemberToOrganization_thenReturnOrganizationIdAndUserId() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aOwnerMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                aOrganization.getId(),
                aAuthenticatedUser.getId()
        );
        final var aRole = OrganizationMemberRole.MEMBER;

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aOwnerMember));
        Mockito.when(organizationMemberRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aOrganization.getId().value().toString(), aOutput.organizationId());
        Assertions.assertEquals(aAddUser.getId().value().toString(), aOutput.addedUserId());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.equals(aOrganization.getId(), aCmd.getOrganizationId())
                        && Objects.equals(aAddUser.getId(), aCmd.getUserId())
                        && Objects.equals(aRole, aCmd.getMemberRole())));
    }

    @Test
    void givenAnNonExistsOrganizationId_whenCallAddMemberToOrganization_thenThrowsNotFoundException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.MEMBER;

        final var expectedErrorMessage = "Organization with id %s was not found".formatted(aOrganization.getId().value().toString());

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(false);

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(0)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnNonExistsAddUserId_whenCallAddMemberToOrganization_thenThrowsNotFoundException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.MEMBER;

        final var expectedErrorMessage = "User with id %s was not found".formatted(aAddUser.getId().value().toString());

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(false);

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnNonExistsAuthenticatedMemberToOrganization_whenCallAddMemberToOrganization_thenThrowsNotFoundException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.MEMBER;

        final var expectedErrorMessage = "OrganizationMember with id %s was not found".formatted(aAuthenticatedUser.getId().value().toString());

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidRoleName_whenCallAddMemberToOrganization_thenThrowNotFoundException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = "non-exists";

        final var expectedErrorMessage = "Role non-exists was not found";

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(Fixture.OrganizationMemberFixture.newOwnerMember(
                        aOrganization.getId(),
                        aAuthenticatedUser.getId()
                )));

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidOwnerRole_whenCallAddMemberToOrganization_thenThrowsDomainException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.OWNER;

        final var expectedErrorMessage = "Only one owner per organization permitted";

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(Fixture.OrganizationMemberFixture.newOwnerMember(
                        aOrganization.getId(),
                        aAuthenticatedUser.getId()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnAuthenticatedUserOrganizationIdDoesNotMatch_whenCallAddMemberToOrganization_thenThrowsDomainException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.MEMBER;

        final var expectedErrorMessage = "The authenticated user must be from the same organization";

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(Fixture.OrganizationMemberFixture.newMember(
                        new OrganizationID(ULID.random()),
                        aAuthenticatedUser.getId(),
                        aRole
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnAuthenticatedUserIsMember_whenCallAddMemberToOrganization_thenThrowsDomainException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.MEMBER;

        final var expectedErrorMessage = "Member does not permitted to add other member to organization";

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(Fixture.OrganizationMemberFixture.newMember(
                        aOrganization.getId(),
                        aAuthenticatedUser.getId(),
                        aRole
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnAuthenticatedUserIsUseSameRoleToAdd_whenCallAddMemberToOrganization_thenThrowsDomainException() {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aAuthenticatedUser = Fixture.UserFixture.newUser();
        final var aAddUser = Fixture.UserFixture.newUser();
        final var aRole = OrganizationMemberRole.ADMIN;

        final var expectedErrorMessage = "Does not permitted add member with same role";

        final var aInput = AddMemberToOrganizationInput.with(
                aOrganization.getId().value().toString(),
                aAuthenticatedUser.getId().value().toString(),
                aAddUser.getId().value().toString(),
                aRole.name()
        );

        Mockito.when(organizationRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(userRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(Fixture.OrganizationMemberFixture.newMember(
                        aOrganization.getId(),
                        aAuthenticatedUser.getId(),
                        aRole
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidInput_whenCallAddMemberToOrganizationUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to AddMemberToOrganizationUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(0)).existsById(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(0)).userOfId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).memberOfUserId(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.times(0)).save(Mockito.any());
    }
}
