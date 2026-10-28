package com.riwi.supportticket.domain.exception.ticket;

import com.riwi.supportticket.domain.exception.DomainException;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;

public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(Long ticketId, TicketStatus from, TicketStatus to) {
        super("Ticket " + ticketId + " cannot move from " + from + " to " + to);
    }
}
