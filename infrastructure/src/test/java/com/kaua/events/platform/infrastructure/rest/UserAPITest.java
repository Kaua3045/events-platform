package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.users.create.CreateUserInput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserOutput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdOutput;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdUseCase;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentInput;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentOutput;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentUseCase;
import com.kaua.events.platform.application.usecases.users.update.phone.UpdateUserPhoneNumberInput;
import com.kaua.events.platform.application.usecases.users.update.phone.UpdateUserPhoneNumberOutput;
import com.kaua.events.platform.application.usecases.users.update.phone.UpdateUserPhoneNumberUseCase;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
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

@ControllerTest(controllers = UserAPI.class)
class UserAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private GetUserByIdUseCase getUserByIdUseCase;

    @MockitoBean
    private UpdateUserDocumentUseCase updateUserDocumentUseCase;

    @MockitoBean
    private UpdateUserPhoneNumberUseCase updateUserPhoneNumberUseCase;

    @Captor
    private ArgumentCaptor<CreateUserInput> createUserInputCaptor;

    @Captor
    private ArgumentCaptor<UpdateUserDocumentInput> updateUserDocumentInputCaptor;

    @Captor
    private ArgumentCaptor<UpdateUserPhoneNumberInput> updateUserPhoneNumberInputCaptor;

    @Test
    void givenAValidRequest_whenCallCreateUser_thenReturnUserId() throws Exception {
        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "teste@tesss.com";
        final var aPassword = "1233546Ab*";

        final var aExpectedUserId = ULID.random().toString();

        Mockito.when(createUserUseCase.execute(any()))
                .thenAnswer(call -> new CreateUserOutput(String.valueOf(aExpectedUserId)));

        var json = """
                {
                    "first_name": "%s",
                    "last_name": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(aFirstName, aLastName, aEmail, aPassword);

        final var aRequest = MockMvcRequestBuilders.post("/v1/users")
                .with(ApiTest.admin())
                .with(csrf())
                .header(IdempotencyKey.IDEMPOTENCY_KEY_HEADER, ULID.random().toString())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/users/me"))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.user_id").value(aExpectedUserId));

        Mockito.verify(createUserUseCase, Mockito.times(1)).execute(createUserInputCaptor.capture());

        final var aCreateUserInput = createUserInputCaptor.getValue();

        Assertions.assertEquals(aFirstName, aCreateUserInput.firstName());
        Assertions.assertEquals(aLastName, aCreateUserInput.lastName());
        Assertions.assertEquals(aEmail, aCreateUserInput.email());
        Assertions.assertEquals(aPassword, aCreateUserInput.password());
    }

    @Test
    void givenAValidAuthenticatedUser_whenCallGetMe_thenReturnAuthenticatedUser() throws Exception {
        final var aUser = Fixture.UserFixture.newUser();
        final var aUserId = aUser.getId().value().toString();

        Mockito.when(getUserByIdUseCase.execute(any()))
                .thenAnswer(call -> GetUserByIdOutput.from(aUser));

        final var aRequest = MockMvcRequestBuilders.get("/v1/users/me")
                .with(ApiTest.admin(aUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(aUserId))
                .andExpect(jsonPath("$.first_name").value(aUser.getName().firstName()))
                .andExpect(jsonPath("$.last_name").value(aUser.getName().lastName()))
                .andExpect(jsonPath("$.email").value(aUser.getEmail().value()))
                .andExpect(jsonPath("$.role").value(aUser.getRole().name()))
                .andExpect(jsonPath("$.created_at").value(aUser.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updated_at").value(aUser.getUpdatedAt().toString()));
    }

    @Test
    void givenAValidRequest_whenCallUpdateDocument_thenReturnUpdatedDocument() throws Exception {
        final var aUser = Fixture.UserFixture.newUser();
        final var aUserId = aUser.getId().value().toString();

        final var aDocumentType = "CPF";
        final var aDocumentNumber = "12345678901";

        final var aOutput = new UpdateUserDocumentOutput(aUserId);

        Mockito.when(updateUserDocumentUseCase.execute(any()))
                .thenReturn(aOutput);

        final var json = """
                {
                    "document_type": "%s",
                    "document_number": "%s"
                }
                """.formatted(aDocumentType, aDocumentNumber);

        final var aRequest = MockMvcRequestBuilders.patch("/v1/users/update/document")
                .with(ApiTest.admin(aUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(aUserId));

        Mockito.verify(updateUserDocumentUseCase, Mockito.times(1))
                .execute(updateUserDocumentInputCaptor.capture());

        final var aCapturedInput = updateUserDocumentInputCaptor.getValue();

        Assertions.assertEquals(aUserId, aCapturedInput.userId());
        Assertions.assertEquals(aDocumentType, aCapturedInput.documentType());
        Assertions.assertEquals(aDocumentNumber, aCapturedInput.documentNumber());
    }

    @Test
    void givenAValidRequest_whenCallUpdatePhone_thenReturnUpdatedPhone() throws Exception {
        final var aUser = Fixture.UserFixture.newUser();
        final var aUserId = aUser.getId().value().toString();

        final var aPhoneNumber = "+5511987654321";
        final var aDefaultRegion = "br";

        final var aOutput = new UpdateUserPhoneNumberOutput(aUserId);

        Mockito.when(updateUserPhoneNumberUseCase.execute(any()))
                .thenReturn(aOutput);

        final var json = """
                {
                    "phone_number": "%s",
                    "default_region": "%s"
                }
                """.formatted(aPhoneNumber, aDefaultRegion);

        final var aRequest = MockMvcRequestBuilders.patch("/v1/users/update/phone")
                .with(ApiTest.admin(aUserId))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(aUserId));

        Mockito.verify(updateUserPhoneNumberUseCase, Mockito.times(1))
                .execute(updateUserPhoneNumberInputCaptor.capture());

        final var aCapturedInput = updateUserPhoneNumberInputCaptor.getValue();

        Assertions.assertEquals(aUserId, aCapturedInput.userId());
        Assertions.assertEquals(aPhoneNumber, aCapturedInput.phoneNumber());
        Assertions.assertEquals(aDefaultRegion, aCapturedInput.defaultRegion());
    }
}
