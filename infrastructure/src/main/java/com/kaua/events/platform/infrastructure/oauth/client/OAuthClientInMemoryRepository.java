package com.kaua.events.platform.infrastructure.oauth.client;

import com.kaua.events.platform.application.repositories.OAuthClientRepository;
import com.kaua.events.platform.domain.auth.OAuthClient;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class OAuthClientInMemoryRepository implements OAuthClientRepository {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientInMemoryRepository.class);

    private final OAuthClients oAuthClients;

    public OAuthClientInMemoryRepository(final OAuthClients oAuthClients) {
        this.oAuthClients = Objects.requireNonNull(oAuthClients);
    }

    @Override
    public Optional<OAuthClient> clientOfClientId(final String clientId) {
        log.debug("Finding client by clientId: {}", clientId);
        return this.oAuthClients.getClient(clientId);
    }
}
