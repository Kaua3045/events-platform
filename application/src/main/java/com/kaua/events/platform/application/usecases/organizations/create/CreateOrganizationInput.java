package com.kaua.events.platform.application.usecases.organizations.create;

public record CreateOrganizationInput(
        String firstName,
        String lastName,
        String email,
        String password,
        String organizationName,
        String description
) {

    public static CreateOrganizationInput with(
            final String aFirstName,
            final String aLastName,
            final String aEmail,
            final String aPassword,
            final String aOrganizationName,
            final String aDescription
    ) {
        return new CreateOrganizationInput(
                aFirstName,
                aLastName,
                aEmail,
                aPassword,
                aOrganizationName,
                aDescription
        );
    }
}
