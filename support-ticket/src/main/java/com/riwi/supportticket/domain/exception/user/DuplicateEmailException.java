package com.riwi.supportticket.domain.exception.user;

import com.riwi.supportticket.domain.exception.DomainException;

public class DuplicateEmailException extends DomainException {

    public DuplicateEmailException(String email) {
        super("Email " + email + " is already registered");
    }
}
