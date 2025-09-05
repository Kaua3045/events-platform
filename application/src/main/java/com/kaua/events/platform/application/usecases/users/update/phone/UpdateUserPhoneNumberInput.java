package com.kaua.events.platform.application.usecases.users.update.phone;

public record UpdateUserPhoneNumberInput(
        String userId,
        String phoneNumber,
        String defaultRegion
) {

    public static UpdateUserPhoneNumberInput with(
            final String aUserId,
            final String aPhoneNumber,
            final String aDefaultRegion
    ) {
        return new UpdateUserPhoneNumberInput(aUserId, aPhoneNumber, aDefaultRegion);
    }
}
