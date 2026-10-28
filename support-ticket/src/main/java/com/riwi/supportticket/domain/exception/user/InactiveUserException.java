package com.riwi.supportticket.domain.exception.user;

import com.riwi.supportticket.domain.exception.DomainException;

public class InactiveUserException extends DomainException {

    public InactiveUserException(Long userId) {
        super("User " + userId + " is inactive");
    }
}
