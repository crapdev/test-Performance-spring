package com.riwi.supportticket.infrastructure.adapter.in.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Va sobre la clase porque compara dos campos: newStatus y observation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStatusChangeValidator.class)
public @interface ValidStatusChange {

    String message() default "is required when the new status is RESUELTO or CANCELADO";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
