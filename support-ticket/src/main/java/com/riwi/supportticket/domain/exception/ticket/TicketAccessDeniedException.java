package com.riwi.supportticket.domain.exception.ticket;

import com.riwi.supportticket.domain.exception.DomainException;

public class TicketAccessDeniedException extends DomainException {

    public TicketAccessDeniedException(Long ticketId) {
        super("You do not have access to ticket " + ticketId);
    }
}
