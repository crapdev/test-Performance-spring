package com.riwi.supportticket.domain.exception.user;

import com.riwi.supportticket.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long userId) {
        super("User " + userId + " not found");
    }
}
