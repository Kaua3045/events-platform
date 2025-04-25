package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.infrastructure.organizations.req.CreateOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.res.CreateOrganizationResponse;
import com.kaua.events.platform.infrastructure.rest.OrganizationAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OrganizationRestController implements OrganizationAPI {

    private final CreateOrganizationUseCase createOrganizationUseCase;

    public OrganizationRestController(final CreateOrganizationUseCase createOrganizationUseCase) {
        this.createOrganizationUseCase = Objects.requireNonNull(createOrganizationUseCase);
    }

    @Override
    public ResponseEntity<CreateOrganizationResponse> creasteOrganization(final CreateOrganizationRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.createOrganizationUseCase.execute(aInput);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateOrganizationResponse.from(aOutput));
    }
}
