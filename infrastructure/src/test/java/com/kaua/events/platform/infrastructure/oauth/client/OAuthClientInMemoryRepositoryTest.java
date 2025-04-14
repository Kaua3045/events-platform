package com.kaua.events.platform.infrastructure.oauth.client;

import com.kaua.events.platform.IntegrationTest;
import com.kaua.events.platform.application.repositories.OAuthClientRepository;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class OAuthClientInMemoryRepositoryTest {

    @Autowired
    private OAuthClientRepository oAuthClientRepository;

    @Autowired
    private OAuthClients oAuthClients;

    @Test
    void givenAValidClientId_whenCallClientOfClientId_thenReturnOAuthClient() {
        final var aClientId = oAuthClients.getClients().values().stream()
                .findFirst()
                .orElseThrow(() -> InternalErrorException.with("Not found default client in tests profile"))
                .clientId();

        final var aOAuthClient = oAuthClientRepository.clientOfClientId(aClientId);

        Assertions.assertTrue(aOAuthClient.isPresent());
        Assertions.assertEquals(aClientId, aOAuthClient.get().clientId());
    }
}
