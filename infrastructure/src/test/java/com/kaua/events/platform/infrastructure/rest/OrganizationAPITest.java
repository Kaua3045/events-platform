package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationInput;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationOutput;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationInput;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationOutput;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdOutput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersOutput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdOutput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberInput;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberOutput;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberUseCase;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = OrganizationAPI.class)
class OrganizationAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateOrganizationUseCase createOrganizationUseCase;

    @MockitoBean
    private AddMemberToOrganizationUseCase addMemberToOrganizationUseCase;

    @MockitoBean
    private GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    @MockitoBean
    private UpdateMemberUseCase updateMemberUseCase;

    @MockitoBean
    private ListOrganizationMembersUseCase listOrganizationMembersUseCase;

    @MockitoBean
    private GetOrganizationMemberByUserIdUseCase getOrganizationMemberByUserIdUseCase;

    @Captor
    private ArgumentCaptor<CreateOrganizationInput> createOrganizationInputCaptor;

    @Captor
    private ArgumentCaptor<AddMemberToOrganizationInput> addMemberInputCaptor;

    @Captor
    private ArgumentCaptor<UpdateMemberInput> updateMemberInputCaptor;

    @Test
    void givenAValidRequest_whenCallCreateOrganization_thenReturnOrganizationId() throws Exception {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "testes@tess.com";
        final var aPassword = "1233546Ab*";
        final var aOrganizationName = "organization-test";
        final var aOrganizationDescription = "teste";

        final var aExpectedUserId = ULID.random().toString();
        final var aExpectedOrganizationId = ULID.random().toString();

        Mockito.when(createOrganizationUseCase.execute(any()))
                .thenAnswer(call -> new CreateOrganizationOutput(aExpectedOrganizationId, aExpectedUserId));

        var json = """
                {
                    "first_name": "%s",
                    "last_name": "%s",
                    "email": "%s",
                    "password": "%s",
                    "organization_name": "%s",
                    "description": "%s"
                }
                """.formatted(aFirstName, aLastName, aEmail, aPassword, aOrganizationName, aOrganizationDescription);

        final var aRequest = MockMvcRequestBuilders.post("/v1/organizations")
                .with(ApiTest.admin())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.owner_id").value(aExpectedUserId))
                .andExpect(jsonPath("$.organization_id").value(aExpectedOrganizationId));

        Mockito.verify(createOrganizationUseCase, Mockito.times(1)).execute(createOrganizationInputCaptor.capture());

        final var aCreateOrganizationInput = createOrganizationInputCaptor.getValue();

        Assertions.assertEquals(aFirstName, aCreateOrganizationInput.firstName());
        Assertions.assertEquals(aLastName, aCreateOrganizationInput.lastName());
        Assertions.assertEquals(aEmail, aCreateOrganizationInput.email());
        Assertions.assertEquals(aPassword, aCreateOrganizationInput.password());
        Assertions.assertEquals(aOrganizationName, aCreateOrganizationInput.organizationName());
        Assertions.assertEquals(aOrganizationDescription, aCreateOrganizationInput.description());
    }

    @Test
    void givenAValidRequest_whenCallAddMemberToOrganization_thenReturnOrganizationIdAndUserId() throws Exception {
        final var aOrganizationId = ULID.random().toString();
        final var aAuthenticatedUserId = ULID.random().toString();
        final var aAddUserId = ULID.random().toString();
        final var aRole = "MEMBER";

        Mockito.when(addMemberToOrganizationUseCase.execute(any()))
                .thenAnswer(call -> new AddMemberToOrganizationOutput(aOrganizationId, aAddUserId));

        var json = """
                {
                    "organization_id": "%s",
                    "authenticated_user_id": "%s",
                    "add_user_id": "%s",
                    "role": "%s"
                }
                """.formatted(aOrganizationId, aAuthenticatedUserId, aAddUserId, aRole);

        final var aRequest = MockMvcRequestBuilders.post("/v1/organizations/add-member")
                .with(ApiTest.admin(aAuthenticatedUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.added_user_id").value(aAddUserId))
                .andExpect(jsonPath("$.organization_id").value(aOrganizationId));

        Mockito.verify(addMemberToOrganizationUseCase, Mockito.times(1)).execute(addMemberInputCaptor.capture());

        final var aAddMemberToOrganization = addMemberInputCaptor.getValue();

        Assertions.assertEquals(aOrganizationId, aAddMemberToOrganization.organizationId());
        Assertions.assertEquals(aAuthenticatedUserId, aAddMemberToOrganization.authenticatedUserId());
        Assertions.assertEquals(aAddUserId, aAddMemberToOrganization.userId());
        Assertions.assertEquals(aRole, aAddMemberToOrganization.role());
    }

    @Test
    void givenAValidRequest_whenCallGetOrganizationBy_thenReturnOrganization() throws Exception {
        final var aOrganization = Fixture.OrganizationFixture.newOrganization();
        final var aId = aOrganization.getId().value().toString();

        Mockito.when(getOrganizationByIdUseCase.execute(any()))
                .thenAnswer(call -> GetOrganizationByIdOutput.from(aOrganization));

        final var aRequest = MockMvcRequestBuilders.get("/v1/organizations/" + aId)
                .with(ApiTest.admin())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(aId))
                .andExpect(jsonPath("$.version").value(aOrganization.getVersion()))
                .andExpect(jsonPath("$.name").value(aOrganization.getName()))
                .andExpect(jsonPath("$.description").value(aOrganization.getDescription().get()))
                .andExpect(jsonPath("$.is_deleted").value(aOrganization.isDeleted()))
                .andExpect(jsonPath("$.created_at").value(aOrganization.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updated_at").value(aOrganization.getUpdatedAt().toString()))
                .andExpect(jsonPath("$.deleted_at").value(aOrganization.getDeletedAt().orElse(null)));

        Mockito.verify(getOrganizationByIdUseCase, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void givenAValidRequest_whenCallUpdateMember_thenReturnUserId() throws Exception {
        final var aAuthenticatedUserId = ULID.random().toString();
        final var aAddUserId = ULID.random().toString();
        final var aRole = "MEMBER";

        Mockito.when(updateMemberUseCase.execute(any()))
                .thenAnswer(call -> new UpdateMemberOutput(aAddUserId));

        var json = """
                {
                    "authenticated_user_id": "%s",
                    "update_user_id": "%s",
                    "role": "%s"
                }
                """.formatted(aAuthenticatedUserId, aAddUserId, aRole);

        final var aRequest = MockMvcRequestBuilders.patch("/v1/organizations/update")
                .with(ApiTest.admin(aAuthenticatedUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.updated_user_id").value(aAddUserId));

        Mockito.verify(updateMemberUseCase, Mockito.times(1)).execute(updateMemberInputCaptor.capture());

        final var aInputUpdate = updateMemberInputCaptor.getValue();

        Assertions.assertEquals(aAuthenticatedUserId, aInputUpdate.authenticatedUserId());
        Assertions.assertEquals(aAddUserId, aInputUpdate.userId());
        Assertions.assertEquals(aRole, aInputUpdate.roleName());
    }

    @Test
    void givenAValidValues_whenCallListOrganizationMembers_thenReturnMembersPaginated() throws Exception {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aMemberOne = Fixture.OrganizationMemberFixture.newOwnerMember(aOrganizationId, new UserID(ULID.random()));
        final var aMemberTwo = Fixture.OrganizationMemberFixture.newMember(
                aOrganizationId, new UserID(ULID.random()), OrganizationMemberRole.MEMBER
        );

        final var aPage = 0;
        final var aPerPage = 2;
        final var aTerms = "";
        final var aSort = "created_at";
        final var aDirection = "asc";
        final var aItemsCount = 2;
        final var aPagesCount = 1;

        final var aItems = List.of(ListOrganizationMembersOutput.from(aMemberOne), ListOrganizationMembersOutput.from(aMemberTwo));
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aPagesCount, aItemsCount);

        Mockito.when(listOrganizationMembersUseCase.execute(any()))
                .thenReturn(new Pagination<>(aMetadata, aItems));

        final var aRequest = MockMvcRequestBuilders.get("/v1/organizations/members")
                .with(ApiTest.admin(ULID.random().toString()))
                .queryParam("organizationId", aOrganizationId.value().toString())
                .queryParam("page", String.valueOf(aPage))
                .queryParam("perPage", String.valueOf(aPerPage))
                .queryParam("search", aTerms)
                .queryParam("sort", aSort)
                .queryParam("direction", aDirection)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.metadata.current_page").value(aPage))
                .andExpect(jsonPath("$.metadata.per_page").value(aPerPage))
                .andExpect(jsonPath("$.metadata.total_pages").value(aPagesCount))
                .andExpect(jsonPath("$.metadata.total_items").value(aItemsCount))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].member_id").value(aMemberOne.getId().value().toString()))
                .andExpect(jsonPath("$.items[0].user_id").value(aMemberOne.getUserId().value().toString()))
                .andExpect(jsonPath("$.items[0].role").value(aMemberOne.getMemberRole().name()))
                .andExpect(jsonPath("$.items[0].created_at").value(aMemberOne.getCreatedAt().toString()))
                .andExpect(jsonPath("$.items[1].member_id").value(aMemberTwo.getId().value().toString()));

        Mockito.verify(listOrganizationMembersUseCase, Mockito.times(1)).execute(any());
    }

    @Test
    void givenAValidUserId_whenCallGetOrganizationMemberByUserId_thenReturnMember() throws Exception {
        final var aUserId = ULID.random();
        final var aOrganizationId = ULID.random();
        final var aMemberRole = OrganizationMemberRole.OWNER;

        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId), new UserID(aUserId)
        );

        Mockito.when(getOrganizationMemberByUserIdUseCase.execute(any()))
                .thenAnswer(call -> GetOrganizationMemberByUserIdOutput.from(aMember));

        final var aRequest = MockMvcRequestBuilders.get("/v1/organizations/members/" + aUserId)
                .with(ApiTest.admin())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.organization_member_id").value(aMember.getId().value().toString()))
                .andExpect(jsonPath("$.version").value(aMember.getVersion()))
                .andExpect(jsonPath("$.organization_id").value(aOrganizationId.toString()))
                .andExpect(jsonPath("$.user_id").value(aUserId.toString()))
                .andExpect(jsonPath("$.member_role").value(aMemberRole.name()))
                .andExpect(jsonPath("$.created_at").value(aMember.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updated_at").value(aMember.getUpdatedAt().toString()));

        Mockito.verify(getOrganizationMemberByUserIdUseCase, Mockito.times(1)).execute(Mockito.any());
    }
}
