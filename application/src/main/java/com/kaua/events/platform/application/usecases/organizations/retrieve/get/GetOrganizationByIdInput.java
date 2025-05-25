package com.kaua.events.platform.application.usecases.organizations.retrieve.get;

public record GetOrganizationByIdInput(String id) {

    public static GetOrganizationByIdInput with(String id) {
        return new GetOrganizationByIdInput(id);
    }
}
