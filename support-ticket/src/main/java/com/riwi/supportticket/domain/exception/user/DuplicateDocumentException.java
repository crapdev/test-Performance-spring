package com.riwi.supportticket.domain.exception.user;

import com.riwi.supportticket.domain.exception.DomainException;

public class DuplicateDocumentException extends DomainException {

    public DuplicateDocumentException(String document) {
        super("Document " + document + " is already registered");
    }
}
