package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.DefaultCreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrganizationUseCaseConfig {

    @Bean
    public CreateOrganizationUseCase createOrganizationUseCase(
            final OrganizationRepository organizationRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final CreateUserUseCase createUserUseCase
    ) {
        return new DefaultCreateOrganizationUseCase(
                organizationRepository,
                organizationMemberRepository,
                createUserUseCase
        );
    }
}
