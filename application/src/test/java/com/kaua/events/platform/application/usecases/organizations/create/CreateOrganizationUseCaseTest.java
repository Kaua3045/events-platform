package com.kaua.events.platform.application.usecases.organizations.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.usecases.users.create.CreateUserOutput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreateOrganizationUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @InjectMocks
    private DefaultCreateOrganizationUseCase useCase;

    @Test
    void givenAValidValues_whenCallCreateOrganizationUseCase_thenReturnOrganizationId() {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@test.com";
        final var aPassword = "123456Am@";
        final var aOrganizationName = "organization-test";
        final var aOrganizationDescription = "teste";

        final var aExpectedUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());

        final var aInput = CreateOrganizationInput.with(
                aFirstName,
                aLastName,
                aEmail,
                aPassword,
                aOrganizationName,
                aOrganizationDescription
        );

        Mockito.when(organizationRepository.existsByName(aOrganizationName))
                .thenReturn(false);
        Mockito.when(createUserUseCase.execute(Mockito.any()))
                .thenReturn(new CreateUserOutput(aExpectedUserId.value().toString()));
        Mockito.when(organizationRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(organizationMemberRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.organizationId());
        Assertions.assertEquals(aExpectedUserId.value().toString(), aOutput.userId());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsByName(aOrganizationName);
        Mockito.verify(createUserUseCase, Mockito.times(1)).execute(argThat(aCmd ->
                Objects.equals(aFirstName, aCmd.firstName())
                        && Objects.equals(aLastName, aCmd.lastName())
                        && Objects.equals(aEmail, aCmd.email())
                        && Objects.equals(aPassword, aCmd.password())));
        Mockito.verify(organizationRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.nonNull(aCmd.getId())
                        && Objects.equals(0L, aCmd.getVersion())
                        && Objects.equals(aOrganizationName, aCmd.getName())
                        && Objects.equals(aOrganizationDescription, aCmd.getDescription().get())
                        && Objects.nonNull(aCmd.getCreatedAt())
                        && Objects.nonNull(aCmd.getUpdatedAt())));
        Mockito.verify(organizationMemberRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.nonNull(aCmd.getId())
                        && Objects.equals(0L, aCmd.getVersion())
                        && Objects.equals(aExpectedUserId, aCmd.getUserId())
                        && Objects.equals(aCmd.getMemberRole(), OrganizationMemberRole.OWNER)
                        && Objects.nonNull(aCmd.getCreatedAt())
                        && Objects.nonNull(aCmd.getUpdatedAt())));

    }

    @Test
    void givenAnInvalidExistsOrganizationName_whenCallCreateOrganizationUseCase_thenThrowDomainException() {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@test.com";
        final var aPassword = "123456Am@";
        final var aOrganizationName = "organization-test";
        final var aOrganizationDescription = "teste";

        final var aExpectedErrorMessage = "The organization name %s already exists".formatted(aOrganizationName);


        final var aInput = CreateOrganizationInput.with(
                aFirstName,
                aLastName,
                aEmail,
                aPassword,
                aOrganizationName,
                aOrganizationDescription
        );

        Mockito.when(organizationRepository.existsByName(aOrganizationName))
                .thenReturn(true);

        final var aException = Assertions.assertThrows(DomainException.class, () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.times(1)).existsByName(aOrganizationName);
        Mockito.verify(createUserUseCase, Mockito.never()).execute(Mockito.any());
        Mockito.verify(organizationRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidInput_whenCallCreateOrganizationUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateOrganizationUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationRepository, Mockito.never()).existsByName(Mockito.any());
        Mockito.verify(createUserUseCase, Mockito.never()).execute(Mockito.any());
        Mockito.verify(organizationRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(organizationMemberRepository, Mockito.never()).save(Mockito.any());
    }
}
