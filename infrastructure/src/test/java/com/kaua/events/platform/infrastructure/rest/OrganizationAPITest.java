package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationInput;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationOutput;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
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

    @Captor
    private ArgumentCaptor<CreateOrganizationInput> createOrganizationInputCaptor;

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
}
