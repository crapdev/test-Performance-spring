package com.riwi.supportticket.domain.exception.ticket;

import com.riwi.supportticket.domain.exception.DomainException;

public class TicketNotFoundException extends DomainException {

    public TicketNotFoundException(Long ticketId) {
        super("Ticket " + ticketId + " not found");
    }
}
