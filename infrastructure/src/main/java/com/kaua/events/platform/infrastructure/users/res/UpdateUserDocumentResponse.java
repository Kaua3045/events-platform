package com.kaua.events.platform.infrastructure.users.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentOutput;

public record UpdateUserDocumentResponse(
        @JsonProperty("id") String id
) {

    public static UpdateUserDocumentResponse from(final UpdateUserDocumentOutput aOutput) {
        return new UpdateUserDocumentResponse(aOutput.userId());
    }
}
