package com.riwi.supportticket.domain.exception;

// Base de todos los errores de negocio. Infraestructura la traduce a ProblemDetail.
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
