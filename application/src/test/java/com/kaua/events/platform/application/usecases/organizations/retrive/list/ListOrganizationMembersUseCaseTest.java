package com.kaua.events.platform.application.usecases.organizations.retrive.list;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.DefaultListOrganizationMembersUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersInput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersOutput;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

class ListOrganizationMembersUseCaseTest extends UseCaseTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultListOrganizationMembersUseCase useCase;

    @Test
    void givenAnInvalidNullInput_whenCallListOrganizationMembersUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var expectedErrorMessage = "Input to ListOrganizationMembersUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verifyNoInteractions(organizationMemberRepository);
    }

    @Test
    void givenAValidInput_whenCallListOrganizationMembersUseCase_thenReturnOrganizationMembersByOrgIdAndQuery() {
        final var aOrganizationId = ULID.random();

        final var aMemberOne = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId), new UserID(ULID.random()));
        final var aMemberTwo = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(aOrganizationId), new UserID(ULID.random()), OrganizationMemberRole.ADMIN
        );

        final var aMembers = List.of(aMemberOne, aMemberTwo);

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "role";
        final var aDirection = "desc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, aMembers.size());
        final var aPagination = new Pagination<>(aMetadata, aMembers);

        final var aItemsCount = 2;
        final var aResult = aPagination.map(ListOrganizationMembersOutput::from);

        final var aInput = ListOrganizationMembersInput.with(aOrganizationId.toString(), aSearchQuery);

        Mockito.when(organizationMemberRepository.membersOfOrganizationId(aOrganizationId.toString(),
                aSearchQuery)).thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aInput);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertEquals(aResult.items(), aOutput.items());
        Assertions.assertEquals(aResult.metadata(), aOutput.metadata());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).membersOfOrganizationId(aOrganizationId.toString(), aSearchQuery);
    }

    @Test
    void givenAValidInputButHasNoData_whenCallListOrganizationMembersUseCase_thenReturnEmptyData() {
        final var aOrganizationId = ULID.random();

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "role";
        final var aDirection = "asc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, 0);
        final var aPagination = new Pagination<OrganizationMember>(aMetadata, List.of());

        final var aItemsCount = 0;

        final var aInput = ListOrganizationMembersInput.with(aOrganizationId.toString(), aSearchQuery);

        Mockito.when(organizationMemberRepository.membersOfOrganizationId(aOrganizationId.toString(), aSearchQuery))
                .thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aInput);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertTrue(aOutput.items().isEmpty());
        Assertions.assertEquals(aMetadata, aOutput.metadata());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).membersOfOrganizationId(aOrganizationId.toString(), aSearchQuery);
    }
}
