package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.infrastructure.organizations.req.AddMemberToOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.req.CreateOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.res.AddMemberToOrganizationResponse;
import com.kaua.events.platform.infrastructure.organizations.res.CreateOrganizationResponse;
import com.kaua.events.platform.infrastructure.rest.OrganizationAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OrganizationRestController implements OrganizationAPI {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final AddMemberToOrganizationUseCase addMemberToOrganizationUseCase;

    public OrganizationRestController(
            final CreateOrganizationUseCase createOrganizationUseCase,
            final AddMemberToOrganizationUseCase addMemberToOrganizationUseCase
    ) {
        this.createOrganizationUseCase = Objects.requireNonNull(createOrganizationUseCase);
        this.addMemberToOrganizationUseCase = Objects.requireNonNull(addMemberToOrganizationUseCase);
    }

    @Override
    public ResponseEntity<CreateOrganizationResponse> createOrganization(final CreateOrganizationRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.createOrganizationUseCase.execute(aInput);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateOrganizationResponse.from(aOutput));
    }

    @Override
    public ResponseEntity<AddMemberToOrganizationResponse> addMemberToOrganization(final AddMemberToOrganizationRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.addMemberToOrganizationUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AddMemberToOrganizationResponse.from(aOutput));
    }
}
