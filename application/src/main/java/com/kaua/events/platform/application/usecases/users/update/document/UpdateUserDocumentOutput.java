package com.kaua.events.platform.application.usecases.users.update.document;

import com.kaua.events.platform.domain.users.User;

public record UpdateUserDocumentOutput(String userId) {

    public static UpdateUserDocumentOutput from(final User aUser) {
        return new UpdateUserDocumentOutput(aUser.getId().value().toString());
    }
}
