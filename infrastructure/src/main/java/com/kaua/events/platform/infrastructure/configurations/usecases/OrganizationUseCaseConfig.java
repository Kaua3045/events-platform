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
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.DefaultListOrganizationMembersUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.DefaultGetOrganizationMemberByUserIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.update.member.DefaultUpdateMemberUseCase;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberUseCase;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrganizationUseCaseConfig {

    @Bean
    public CreateOrganizationUseCase createOrganizationUseCase(
            final OrganizationRepository organizationRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final CreateUserUseCase createUserUseCase,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultCreateOrganizationUseCase(
                organizationRepository,
                organizationMemberRepository,
                createUserUseCase,
                tracerWrapper
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

    @Bean
    public UpdateMemberUseCase updateMemberUseCase(
            final OrganizationMemberRepository organizationMemberRepository,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultUpdateMemberUseCase(
                organizationMemberRepository,
                tracerWrapper
        );
    }

    @Bean
    public ListOrganizationMembersUseCase listOrganizationMembersUseCase(
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        return new DefaultListOrganizationMembersUseCase(organizationMemberRepository);
    }

    @Bean
    public GetOrganizationMemberByUserIdUseCase getOrganizationMemberByUserIdUseCase(
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        return new DefaultGetOrganizationMemberByUserIdUseCase(organizationMemberRepository);
    }
}
