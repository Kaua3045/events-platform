package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.application.usecases.users.create.DefaultCreateUserUseCase;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserUseCaseConfig {

    @Bean
    public CreateUserUseCase createUserUseCase(
            PasswordEncryption passwordEncryption,
            UserRepository userRepository
    ) {
        return new DefaultCreateUserUseCase(userRepository, passwordEncryption);
    }
}
