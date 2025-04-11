package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ApiTest;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.usecases.users.create.CreateUserInput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserOutput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
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

@ControllerTest(controllers = UserAPI.class)
class UserAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @Captor
    private ArgumentCaptor<CreateUserInput> createUserInputCaptor;

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
}
