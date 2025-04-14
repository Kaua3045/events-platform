package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.auth.OAuthClient;

import java.util.Optional;

public interface OAuthClientRepository {

    Optional<OAuthClient> clientOfClientId(String clientId);
}
