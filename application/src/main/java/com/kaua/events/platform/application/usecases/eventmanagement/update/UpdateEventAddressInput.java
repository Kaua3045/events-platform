package com.kaua.events.platform.application.usecases.eventmanagement.update;

public record UpdateEventAddressInput(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String postalCode,
        String country
) {

    public static UpdateEventAddressInput with(
            final String street,
            final String number,
            final String complement,
            final String neighborhood,
            final String city,
            final String state,
            final String postalCode,
            final String country
    ) {
        return new UpdateEventAddressInput(
                street,
                number,
                complement,
                neighborhood,
                city,
                state,
                postalCode,
                country
        );
    }
}
