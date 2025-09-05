package com.kaua.events.platform.infrastructure.services.token;

import com.kaua.events.platform.IntegrationTest;
import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.*;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

@IntegrationTest
class TokenGeneratorGatewayImplTest {

    @Autowired
    private TokenGeneratorGateway tokenGeneratorGateway;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void givenAValidInputWithTypeIsAccessToken_whenCallGenerateToken_thenReturnAccessToken() {
        final var aClientId = "default";
        final var aSub = "01JRP066XMA9GZZZZHAZZZZZYF";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;

        final var aTokenInput = new TokenGeneratorGateway.TokenInput(aClientId, aSub, aType);

        Mockito.when(userRepository.userOfId(aSub))
                .thenReturn(Optional.of(User.with(
                        new UserID(ULID.fromString(aSub)),
                        0L,
                        new Name("john", "doe"),
                        new Email("john.doe@gmail.com"),
                        Password.of("12345677Am@"),
                        UserRole.USER,
                        null,
                        null,
                        InstantUtils.now(),
                        InstantUtils.now()
                )));

        final var aToken = this.tokenGeneratorGateway.generateToken(aTokenInput);

        Assertions.assertNotNull(aToken);
        Assertions.assertEquals(aClientId, aToken.clientId());
        Assertions.assertEquals(aSub, aToken.sub());
        Assertions.assertEquals(aType, aToken.type());
        Assertions.assertNotNull(aToken.tokenValue());
        Assertions.assertNotNull(aToken.tokenJTI());
        Assertions.assertNotNull(aToken.expiresIn());
        Assertions.assertNotNull(aToken.issuedAt());
        Assertions.assertTrue(aToken.expiresIn().isAfter(aToken.issuedAt()));
    }

    @Test
    void givenAValidInputWithTypeIsRefreshToken_whenCallGenerateToken_thenReturnRefreshToken() {
        final var aClientId = "default";
        final var aSub = "sub";
        final var aType = AuthorizationTokenType.REFRESH_TOKEN;

        final var aTokenInput = new TokenGeneratorGateway.TokenInput(aClientId, aSub, aType);

        final var aToken = this.tokenGeneratorGateway.generateToken(aTokenInput);

        Assertions.assertNotNull(aToken);
        Assertions.assertEquals(aClientId, aToken.clientId());
        Assertions.assertEquals(aSub, aToken.sub());
        Assertions.assertEquals(aType, aToken.type());
        Assertions.assertNotNull(aToken.tokenValue());
        Assertions.assertNotNull(aToken.tokenJTI());
        Assertions.assertNotNull(aToken.expiresIn());
        Assertions.assertNotNull(aToken.issuedAt());
        Assertions.assertTrue(aToken.expiresIn().isAfter(aToken.issuedAt()));
    }

    @Test
    void givenAnInvalidNullInput_whenCallGenerateToken_thenThrowException() {
        final var aExpectedErrorMessage = "input token generator cannot be null";

        final var aException = Assertions.assertThrows(InternalErrorException.class, () -> {
            this.tokenGeneratorGateway.generateToken(null);
        });

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidInputWithTypeIsAccessTokenAndIsService_whenCallGenerateToken_thenReturnAccessToken() {
        final var aClientId = "default";
        final var aSub = "default";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;

        final var aTokenInput = new TokenGeneratorGateway.TokenInput(aClientId, aSub, aType);

        final var aToken = this.tokenGeneratorGateway.generateToken(aTokenInput);

        Assertions.assertNotNull(aToken);
        Assertions.assertEquals(aClientId, aToken.clientId());
        Assertions.assertEquals(aSub, aToken.sub());
        Assertions.assertEquals(aType, aToken.type());
        Assertions.assertNotNull(aToken.tokenValue());
        Assertions.assertNotNull(aToken.tokenJTI());
        Assertions.assertNotNull(aToken.expiresIn());
        Assertions.assertNotNull(aToken.issuedAt());
        Assertions.assertTrue(aToken.expiresIn().isAfter(aToken.issuedAt()));
    }

    @Test
    void givenAnInvalidClientId_whenCallGenerateToken_thenThrowException() {
        final var aClientId = "nonexistent";
        final var aSub = "01JRP066XMA9GZZZZHAZZZZZYF";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;

        final var expectedErrorMessage = "Client not found";

        final var aTokenInput = new TokenGeneratorGateway.TokenInput(aClientId, aSub, aType);

        Mockito.when(userRepository.userOfId(aSub))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class, () -> {
            this.tokenGeneratorGateway.generateToken(aTokenInput);
        });

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());
    }
}
