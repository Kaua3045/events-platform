package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get;

import com.kaua.events.platform.domain.eventmanagement.Address;

public record GetEventByIdAddressOutput(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String postalCode,
        String country
) {

    public static GetEventByIdAddressOutput from(final Address aAddress) {
        return new GetEventByIdAddressOutput(
                aAddress.getStreet(),
                aAddress.getNumber(),
                aAddress.getComplement().orElse(null),
                aAddress.getNeighborhood(),
                aAddress.getCity(),
                aAddress.getState(),
                aAddress.getPostalCode(),
                aAddress.getCountry()
        );
    }
}
