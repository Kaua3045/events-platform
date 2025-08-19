package com.kaua.events.platform.domain.person;

import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.utils.CnpjUtils;
import com.kaua.events.platform.domain.utils.CpfUtils;

public final class DocumentFactory {

    private DocumentFactory() {}

    public static Document create(final String documentNumber, final String documentType) {
        return switch (documentType.toUpperCase()) {
            case Document.Cpf.DOCUMENT_TYPE -> new Document.Cpf(CpfUtils.cleanCpf(documentNumber));
            case Document.Cnpj.DOCUMENT_TYPE -> new Document.Cnpj(CnpjUtils.cleanCnpj(documentNumber));
            default -> throw DomainException.with("Invalid document type");
        };
    }
}
