package com.kaua.events.platform.infrastructure.eventmanagement.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get.GetEventByIdAddressOutput;

public record GetEventByIdAddressResponse(
        @JsonProperty("street") String street,
        @JsonProperty("number") String number,
        @JsonProperty("complement") String complement,
        @JsonProperty("neighborhood") String neighborhood,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("country") String country
) {

    public static GetEventByIdAddressResponse from(final GetEventByIdAddressOutput aAddress) {
        return new GetEventByIdAddressResponse(
                aAddress.street(),
                aAddress.number(),
                aAddress.complement(),
                aAddress.neighborhood(),
                aAddress.city(),
                aAddress.state(),
                aAddress.postalCode(),
                aAddress.country()
        );
    }
}
