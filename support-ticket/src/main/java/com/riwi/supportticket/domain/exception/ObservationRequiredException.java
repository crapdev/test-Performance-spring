package com.riwi.supportticket.domain.exception;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;

public class ObservationRequiredException extends DomainException {

    public ObservationRequiredException(TicketStatus status) {
        super("An observation is required to move a ticket to " + status);
    }
}
