package com.kaua.events.platform.infrastructure.configurations.usecases;

import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.addMember.DefaultAddMemberToOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.DefaultCreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.DefaultGetOrganizationByIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdUseCase;
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

    @Bean
    public AddMemberToOrganizationUseCase addMemberToOrganizationUseCase(
            final OrganizationMemberRepository organizationMemberRepository,
            final OrganizationRepository organizationRepository,
            final UserRepository userRepository
    ) {
        return new DefaultAddMemberToOrganizationUseCase(
                organizationMemberRepository,
                organizationRepository,
                userRepository
        );
    }

    @Bean
    public GetOrganizationByIdUseCase getOrganizationByIdUseCase(
            final OrganizationRepository organizationRepository
    ) {
        return new DefaultGetOrganizationByIdUseCase(organizationRepository);
    }
}
