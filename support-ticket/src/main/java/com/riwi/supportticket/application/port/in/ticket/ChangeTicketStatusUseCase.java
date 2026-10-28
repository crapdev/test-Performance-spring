package com.riwi.supportticket.application.port.in.ticket;

import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.user.Actor;

public interface ChangeTicketStatusUseCase {

    Ticket changeStatus(Actor actor, Long ticketId, ChangeTicketStatusCommand command);
}
