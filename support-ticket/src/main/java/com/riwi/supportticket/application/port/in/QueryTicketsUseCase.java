package com.riwi.supportticket.application.port.in;

import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.user.Actor;

import java.util.List;

// Lo que ve cada rol: USUARIO sus tickets, TECNICO los asignados a él, ADMIN todos
public interface QueryTicketsUseCase {

    List<Ticket> findAll(Actor actor);

    Ticket findById(Actor actor, Long ticketId);

    List<TicketHistory> findHistory(Actor actor, Long ticketId);
}
