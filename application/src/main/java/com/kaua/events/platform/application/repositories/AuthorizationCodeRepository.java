package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.auth.code.AuthorizationCode;

import java.util.Optional;

public interface AuthorizationCodeRepository {

    Optional<AuthorizationCode> authorizationCodeOfCode(String code);

    AuthorizationCode save(AuthorizationCode authorizationCode);
}
