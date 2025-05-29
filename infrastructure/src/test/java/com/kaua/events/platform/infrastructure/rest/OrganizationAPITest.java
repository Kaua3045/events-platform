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
import com.kaua.events.platform.domain.Fixture;
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

    @Captor
    private ArgumentCaptor<CreateOrganizationInput> createOrganizationInputCaptor;

    @Captor
    private ArgumentCaptor<AddMemberToOrganizationInput> addMemberInputCaptor;

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
}
