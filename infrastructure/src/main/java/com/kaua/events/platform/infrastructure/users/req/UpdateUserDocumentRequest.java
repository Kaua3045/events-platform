package com.kaua.events.platform.infrastructure.users.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.update.document.UpdateUserDocumentInput;

public record UpdateUserDocumentRequest(
        @JsonProperty("document_number") String documentNumber,
        @JsonProperty("document_type") String documentType
) {

    public UpdateUserDocumentInput toInput(
            final String aUserId,
            final String aDocumentNumber,
            final String aDocumentType
    ) {
        return new UpdateUserDocumentInput(aUserId, aDocumentNumber, aDocumentType);
    }
}
