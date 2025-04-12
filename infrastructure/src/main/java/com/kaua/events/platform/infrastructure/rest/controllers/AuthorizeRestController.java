package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.auth.code.create.CreateAuthorizationCodeInput;
import com.kaua.events.platform.application.usecases.auth.code.create.CreateAuthorizationCodeUseCase;
import com.kaua.events.platform.application.usecases.auth.token.create.AuthorizationCodeGrantInput;
import com.kaua.events.platform.application.usecases.auth.token.create.CreateAuthorizationTokenUseCase;
import com.kaua.events.platform.application.usecases.auth.token.create.RefreshTokenGrantInput;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import com.kaua.events.platform.infrastructure.oauth.code.req.CreateAuthorizationCodeRequest;
import com.kaua.events.platform.infrastructure.oauth.code.res.CreateAuthorizationCodeResponse;
import com.kaua.events.platform.infrastructure.rest.AuthorizeAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class AuthorizeRestController implements AuthorizeAPI {

    private final CreateAuthorizationCodeUseCase createAuthorizationCodeUseCase;
    private final CreateAuthorizationTokenUseCase createAuthorizationTokenUseCase;
    private final OAuthClients oAuthClients;

    public AuthorizeRestController(
            final CreateAuthorizationCodeUseCase createAuthorizationCodeUseCase,
            final CreateAuthorizationTokenUseCase createAuthorizationTokenUseCase,
            final OAuthClients oAuthClients
    ) {
        this.createAuthorizationCodeUseCase = Objects.requireNonNull(createAuthorizationCodeUseCase);
        this.createAuthorizationTokenUseCase = Objects.requireNonNull(createAuthorizationTokenUseCase);
        this.oAuthClients = Objects.requireNonNull(oAuthClients);
    }

    @Override
    public ResponseEntity<CreateAuthorizationCodeResponse> createAuthorizationCode(
            final CreateAuthorizationCodeRequest request
    ) {
        final var aOAuthClient = this.oAuthClients.getClients().get(request.clientId());

        final var aInput = new CreateAuthorizationCodeInput(
                aOAuthClient.clientId(),
                aOAuthClient.redirectUri(),
                request.codeChallenge(),
                request.codeChallengeMethod(),
                request.email(),
                request.password()
        );

        final var aOutput = this.createAuthorizationCodeUseCase.execute(aInput);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAuthorizationCodeResponse(
                        aOutput.code(),
                        aOutput.redirectUri()
                ));
    }

    @Override
    public ResponseEntity<?> createToken(
            final String code,
            final String clientId,
            final String clientSecret,
            final String codeVerifier
    ) {
        final var aOAuthClient = this.oAuthClients.getClients().get(clientId);

        if (aOAuthClient == null) { // TODO verify this code
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_client"));
        }

        final var aOutput = this.createAuthorizationTokenUseCase.execute(
                new AuthorizationCodeGrantInput(aOAuthClient.clientId(), code, codeVerifier)
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "access_token", aOutput.accessToken().tokenValue(),
                        "token_type", "Bearer",
                        "expires_in", aOutput.accessToken().expiresIn(),
                        "refresh_token", aOutput.refreshToken().tokenValue()
                ));
    }

    @Override
    public ResponseEntity<?> refreshToken(
            final String refreshToken,
            final String clientId
    ) {
        final var aOAuthClient = this.oAuthClients.getClients().get(clientId);

        if (aOAuthClient == null) { // TODO verify this code
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_client"));
        }

        final var aOutput = this.createAuthorizationTokenUseCase.execute(
                new RefreshTokenGrantInput(aOAuthClient.clientId(), refreshToken)
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "access_token", aOutput.accessToken().tokenValue(),
                        "token_type", "Bearer",
                        "expires_in", aOutput.accessToken().expiresIn(),
                        "refresh_token", aOutput.refreshToken().tokenValue()
                ));
    }
}
