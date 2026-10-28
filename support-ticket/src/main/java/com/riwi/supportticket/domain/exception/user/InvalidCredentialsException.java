package com.riwi.supportticket.domain.exception.user;

import com.riwi.supportticket.domain.exception.DomainException;

// Mismo mensaje para correo inexistente y clave mala: así no se revela qué correos existen
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
