package com.kaua.events.platform.infrastructure.eventmanagement.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.update.UpdateEventAddressInput;

public record UpdateEventAddressRequest(
        @JsonProperty("street") String street,
        @JsonProperty("number") String number,
        @JsonProperty("complement") String complement,
        @JsonProperty("neighborhood") String neighborhood,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("country") String country
) {

    public UpdateEventAddressInput toInput() {
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
