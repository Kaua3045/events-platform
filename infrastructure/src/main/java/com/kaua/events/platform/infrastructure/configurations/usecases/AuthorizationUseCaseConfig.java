package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
import com.kaua.events.platform.application.repositories.OAuthClientRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.usecases.auth.code.create.CreateAuthorizationCodeUseCase;
import com.kaua.events.platform.application.usecases.auth.code.create.DefaultCreateAuthorizationCodeUseCase;
import com.kaua.events.platform.application.usecases.auth.token.create.CreateAuthorizationTokenUseCase;
import com.kaua.events.platform.application.usecases.auth.token.create.DefaultCreateAuthorizationTokenUseCase;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuthorizationUseCaseConfig {

    @Bean
    public CreateAuthorizationCodeUseCase createAuthorizationCodeUseCase(
            final AuthorizationCodeRepository authorizationCodeRepository,
            final UserRepository userRepository,
            final PasswordEncryption passwordEncryption,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultCreateAuthorizationCodeUseCase(
                authorizationCodeRepository,
                userRepository,
                passwordEncryption,
                tracerWrapper
        );
    }

    @Bean
    public CreateAuthorizationTokenUseCase createAuthorizationTokenUseCase(
            final AuthorizationTokenRepository authorizationTokenRepository,
            final AuthorizationCodeRepository authorizationCodeRepository,
            final OAuthClientRepository oAuthClientRepository,
            final TokenGeneratorGateway tokenGeneratorGateway,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultCreateAuthorizationTokenUseCase(
                authorizationTokenRepository,
                authorizationCodeRepository,
                oAuthClientRepository,
                tokenGeneratorGateway,
                tracerWrapper
        );
    }
}
