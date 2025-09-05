package com.kaua.events.platform.application.usecases.users.update.document;

public record UpdateUserDocumentInput(
        String userId,
        String documentNumber,
        String documentType
) {

    public static UpdateUserDocumentInput with(
            final String aUserId,
            final String aDocumentNumber,
            final String aDocumentType
    ) {
        return new UpdateUserDocumentInput(aUserId, aDocumentNumber, aDocumentType);
    }
}
