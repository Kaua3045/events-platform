package com.kaua.events.platform.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.events.platform.ControllerTest;
import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.usecases.auth.code.create.CreateAuthorizationCodeOutput;
import com.kaua.events.platform.application.usecases.auth.code.create.CreateAuthorizationCodeUseCase;
import com.kaua.events.platform.application.usecases.auth.token.create.AuthorizationCodeGrantInput;
import com.kaua.events.platform.application.usecases.auth.token.create.ClientSecretGrantInput;
import com.kaua.events.platform.application.usecases.auth.token.create.CreateAuthorizationTokenOutput;
import com.kaua.events.platform.application.usecases.auth.token.create.CreateAuthorizationTokenUseCase;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(controllers = AuthorizeAPI.class)
class AuthorizeAPITest {

    @Autowired
    private OAuthClients oAuthClients;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateAuthorizationCodeUseCase createAuthorizationCodeUseCase;

    @MockitoBean
    private CreateAuthorizationTokenUseCase createAuthorizationTokenUseCase;

    @Test
    void givenAValidRequestCreateCode_whenCallCreateAuthorize_thenReturnCode() throws Exception {
        final var aClientId = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientId();
        final var aClientSecret = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientSecret();
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "codeChallengeMethod";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "123456Ab*";

        final var aAuthBasic = Base64.getEncoder()
                .encodeToString("%s:%s".formatted(aClientId, aClientSecret).getBytes());

        final var aExpectedCode = "code";
        final var aExpectedRedirectUri = oAuthClients.getClients().values().stream().findFirst().orElseThrow().redirectUri()
                .concat("?code=").concat(aExpectedCode);

        Mockito.when(createAuthorizationCodeUseCase.execute(any()))
                .thenAnswer(call -> new CreateAuthorizationCodeOutput(
                        aExpectedCode,
                        aExpectedRedirectUri
                ));

        final var aJson = """
                {
                    "client_id": "%s",
                    "client_secret": "%s",
                    "code_challenge": "%s",
                    "code_challenge_method": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(aClientId, aClientSecret, aCodeChallenge, aCodeChallengeMethod, aEmail, aPassword);

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/code")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(aAuthBasic));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(aJson);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(aExpectedCode))
                .andExpect(jsonPath("$.redirect_uri").value(aExpectedRedirectUri));

        Mockito.verify(createAuthorizationCodeUseCase, Mockito.times(1))
                .execute(any());
    }

    @Test
    void givenAValidRequestTokenWithGrantTypeIsCode_whenCallCreateToken_thenReturnToken() throws Exception {
        final var aClientId = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientId();
        final var aClientSecret = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientSecret();
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var aAuthBasic = Base64.getEncoder()
                .encodeToString("%s:%s".formatted(aClientId, aClientSecret).getBytes());

        final var aExpectedAccessToken = new TokenGeneratorGateway.Token(
                "accessToken",
                "jti",
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                "sub",
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now()
        );
        final var aExpectedRefreshToken = new TokenGeneratorGateway.Token(
                "refreshToken",
                "jti",
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                "sub",
                InstantUtils.now().plus(30, ChronoUnit.DAYS),
                InstantUtils.now()
        );

        Mockito.when(createAuthorizationTokenUseCase.execute(any()))
                .thenAnswer(call -> new CreateAuthorizationTokenOutput(
                        aExpectedAccessToken,
                        aExpectedRefreshToken
                ));

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/token")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(aAuthBasic));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("code", aCode)
                .param("client_id", aClientId)
                .param("grant_type", AuthorizationCodeGrantInput.GRANT_TYPE)
                .param("client_secret", aClientSecret)
                .param("code_verifier", aCodeVerifier);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(aExpectedAccessToken.tokenValue()))
                .andExpect(jsonPath("$.refresh_token").value(aExpectedRefreshToken.tokenValue()));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.times(1))
                .execute(any());
    }

    @Test
    void givenAValidRequestTokenWithGrantTypeIsClientCredentials_whenCallCreateToken_thenReturnToken() throws Exception {
        final var aClientId = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientId();
        final var aClientSecret = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientSecret();

        final var aExpectedAccessToken = new TokenGeneratorGateway.Token(
                "accessToken",
                "jti",
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aClientId,
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now()
        );
        final var aExpectedRefreshToken = new TokenGeneratorGateway.Token(
                "refreshToken",
                "jti",
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aClientId,
                InstantUtils.now().plus(30, ChronoUnit.DAYS),
                InstantUtils.now()
        );

        Mockito.when(createAuthorizationTokenUseCase.execute(any()))
                .thenAnswer(call -> new CreateAuthorizationTokenOutput(
                        aExpectedAccessToken,
                        aExpectedRefreshToken
                ));

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/token")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(createAuth()));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("grant_type", ClientSecretGrantInput.GRANT_TYPE)
                .param("client_id", aClientId)
                .param("client_secret", aClientSecret);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(aExpectedAccessToken.tokenValue()))
                .andExpect(jsonPath("$.refresh_token").value(aExpectedRefreshToken.tokenValue()));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.times(1))
                .execute(any());
    }

    @Test
    void givenAnInvalidRequestTokenWithGrantTypeIsInvalid_whenCallCreateToken_thenThrowsDomainException() throws Exception {
        final var aClientId = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientId();
        final var aClientSecret = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientSecret();

        final var expectedErrorMessage = "Invalid grant type";

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/token")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(createAuth()));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("grant_type", "invalid")
                .param("client_id", aClientId)
                .param("client_secret", aClientSecret);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.times(0))
                .execute(any());
    }

    @Test
    void givenAValidRequestRefreshToken_whenCallRefreshToken_thenReturnToken() throws Exception {
        final var aClientId = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientId();
        final var aClientSecret = oAuthClients.getClients().values().stream().findFirst().orElseThrow().clientSecret();
        final var aRefreshToken = "refreshToken";

        final var aAuthBasic = Base64.getEncoder()
                .encodeToString("%s:%s".formatted(aClientId, aClientSecret).getBytes());

        final var aExpectedAccessToken = new TokenGeneratorGateway.Token(
                "accessToken",
                "jti",
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                "sub",
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now()
        );
        final var aExpectedRefreshToken = new TokenGeneratorGateway.Token(
                "refreshToken",
                "jti",
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                "sub",
                InstantUtils.now().plus(30, ChronoUnit.DAYS),
                InstantUtils.now()
        );

        Mockito.when(createAuthorizationTokenUseCase.execute(any()))
                .thenAnswer(call -> new CreateAuthorizationTokenOutput(
                        aExpectedAccessToken,
                        aExpectedRefreshToken
                ));

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/refresh")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(aAuthBasic));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("refresh_token", aRefreshToken)
                .param("client_id", aClientId);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(aExpectedAccessToken.tokenValue()))
                .andExpect(jsonPath("$.refresh_token").value(aExpectedRefreshToken.tokenValue()));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.times(1))
                .execute(any());
    }

    @Test
    void givenAnInvalidClientIdInRequestCreateCode_whenCallCreateAuthorize_thenThrowsException() throws Exception {
        final var aClientId = "invalidClientId";
        final var aClientSecret = "invalidClientSecret";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "codeChallengeMethod";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "123456Ab*";

        final var expectedErrorMessage = "Client not found";

        final var aJson = """
                {
                    "client_id": "%s",
                    "client_secret": "%s",
                    "code_challenge": "%s",
                    "code_challenge_method": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(aClientId, aClientSecret, aCodeChallenge, aCodeChallengeMethod, aEmail, aPassword);

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/code")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(createAuth()));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(aJson);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        Mockito.verify(createAuthorizationCodeUseCase, Mockito.never())
                .execute(any());
    }

    @Test
    void givenAnInvalidClientIdInRequestToken_whenCallCreateToken_thenThrowsException() throws Exception {
        final var aClientId = "invalidClientId";
        final var aClientSecret = "invalidClientSecret";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "Client not found";

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/token")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(createAuth()));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("code", aCode)
                .param("client_id", aClientId)
                .param("client_secret", aClientSecret)
                .param("grant_type", AuthorizationCodeGrantInput.GRANT_TYPE)
                .param("code_verifier", aCodeVerifier);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.never())
                .execute(any());
    }

    @Test
    void givenAnInvalidClientIdInRequestRefreshToken_whenCallRefreshToken_thenThrowsException() throws Exception {
        final var aClientId = "invalidClientId";
        final var aRefreshToken = "refreshToken";

        final var expectedErrorMessage = "Client not found";

        final var aRequest = MockMvcRequestBuilders.post("/v1/authorize/refresh")
                .with(post -> {
                    post.addHeader("Authorization", "Basic %s".formatted(createAuth()));
                    return post;
                })
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("refresh_token", aRefreshToken)
                .param("client_id", aClientId);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        Mockito.verify(createAuthorizationTokenUseCase, Mockito.never())
                .execute(any());
    }

    private String createAuth() {
        return Base64.getEncoder()
                .encodeToString("%s:%s".formatted(
                        oAuthClients.getClients()
                                .values()
                                .stream().findFirst().orElseThrow().clientId(),
                        oAuthClients.getClients()
                                .values()
                                .stream().findFirst().orElseThrow().clientSecret()).getBytes());
    }
}
