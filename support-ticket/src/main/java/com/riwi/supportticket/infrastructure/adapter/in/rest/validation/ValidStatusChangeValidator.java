package com.riwi.supportticket.infrastructure.adapter.in.rest.validation;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.ChangeStatusRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStatusChangeValidator implements ConstraintValidator<ValidStatusChange, ChangeStatusRequest> {

    @Override
    public boolean isValid(ChangeStatusRequest request, ConstraintValidatorContext context) {
        if (request == null || request.newStatus() == null) {
            return true; // de eso se encarga @NotNull
        }
        boolean needsObservation = request.newStatus() == TicketStatus.RESUELTO
                || request.newStatus() == TicketStatus.CANCELADO;
        boolean hasObservation = request.observation() != null && !request.observation().isBlank();
        if (!needsObservation || hasObservation) {
            return true;
        }
        // El error se reporta en el campo observation, no en la clase
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("observation")
                .addConstraintViolation();
        return false;
    }
}
