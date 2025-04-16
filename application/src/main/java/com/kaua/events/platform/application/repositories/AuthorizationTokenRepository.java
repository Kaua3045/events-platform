package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.auth.token.AuthorizationToken;

import java.util.List;
import java.util.Optional;

public interface AuthorizationTokenRepository {

    List<AuthorizationToken> tokensOfSub(String sub);

    Optional<AuthorizationToken> tokenOfJti(String jti);

    AuthorizationToken save(AuthorizationToken authorizationToken);
}
