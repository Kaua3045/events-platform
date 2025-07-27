package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdInput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersInput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersUseCase;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdInput;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdUseCase;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberUseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.infrastructure.idempotency.IdempotencyKey;
import com.kaua.events.platform.infrastructure.organizations.req.AddMemberToOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.req.CreateOrganizationRequest;
import com.kaua.events.platform.infrastructure.organizations.req.UpdateMemberRequest;
import com.kaua.events.platform.infrastructure.organizations.res.*;
import com.kaua.events.platform.infrastructure.rest.OrganizationAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OrganizationRestController implements OrganizationAPI {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final AddMemberToOrganizationUseCase addMemberToOrganizationUseCase;
    private final GetOrganizationByIdUseCase getOrganizationByIdUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final ListOrganizationMembersUseCase listOrganizationMembersUseCase;
    private final GetOrganizationMemberByUserIdUseCase getOrganizationMemberByUserIdUseCase;

    public OrganizationRestController(
            final CreateOrganizationUseCase createOrganizationUseCase,
            final AddMemberToOrganizationUseCase addMemberToOrganizationUseCase,
            final GetOrganizationByIdUseCase getOrganizationByIdUseCase,
            final UpdateMemberUseCase updateMemberUseCase,
            final ListOrganizationMembersUseCase listOrganizationMembersUseCase,
            final GetOrganizationMemberByUserIdUseCase getOrganizationMemberByUserIdUseCase
    ) {
        this.createOrganizationUseCase = Objects.requireNonNull(createOrganizationUseCase);
        this.addMemberToOrganizationUseCase = Objects.requireNonNull(addMemberToOrganizationUseCase);
        this.getOrganizationByIdUseCase = Objects.requireNonNull(getOrganizationByIdUseCase);
        this.updateMemberUseCase = Objects.requireNonNull(updateMemberUseCase);
        this.listOrganizationMembersUseCase = Objects.requireNonNull(listOrganizationMembersUseCase);
        this.getOrganizationMemberByUserIdUseCase = Objects.requireNonNull(getOrganizationMemberByUserIdUseCase);
    }

    @IdempotencyKey
    @Override
    public ResponseEntity<CreateOrganizationResponse> createOrganization(final CreateOrganizationRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.createOrganizationUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateOrganizationResponse.from(aOutput));
    }

    @IdempotencyKey
    @Override
    public ResponseEntity<AddMemberToOrganizationResponse> addMemberToOrganization(final AddMemberToOrganizationRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.addMemberToOrganizationUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AddMemberToOrganizationResponse.from(aOutput));
    }

    @IdempotencyKey
    @Override
    public ResponseEntity<UpdateMemberResponse> updateMember(final UpdateMemberRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.updateMemberUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UpdateMemberResponse.from(aOutput));
    }

    @Override
    public ResponseEntity<GetOrganizationByIdResponse> getOrganizationById(final String organizationId) {
        final var aOutput = this.getOrganizationByIdUseCase.execute(GetOrganizationByIdInput.with(organizationId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GetOrganizationByIdResponse.from(aOutput));
    }

    @Override
    public Pagination<ListOrganizationMembersResponse> listOrganizationMembers(
            final String organizationId,
            final String search,
            final int page,
            final int perPage,
            final String sort,
            final String direction
    ) {
        final var aInput = ListOrganizationMembersInput.with(
                organizationId,
                SearchQuery.newSearchQuery(
                        page,
                        perPage,
                        search,
                        sort,
                        direction
                )
        );

        final var aOutput = this.listOrganizationMembersUseCase.execute(aInput);

        return aOutput.map(ListOrganizationMembersResponse::from);
    }

    @Override
    public ResponseEntity<GetOrganizationMemberByUserIdResponse> getOrganizationMemberByUserId(
            final String userId
    ) {
        final var aInput = GetOrganizationMemberByUserIdInput.with(userId);

        final var aOutput = this.getOrganizationMemberByUserIdUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GetOrganizationMemberByUserIdResponse.from(aOutput));
    }
}
