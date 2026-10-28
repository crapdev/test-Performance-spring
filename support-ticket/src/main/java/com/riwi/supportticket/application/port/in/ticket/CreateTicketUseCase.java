package com.riwi.supportticket.application.port.in.ticket;

import com.riwi.supportticket.domain.model.ticket.Ticket;

public interface CreateTicketUseCase {

    // Crea el ticket y trata de asignarlo con el dispatch. Devuelve el ticket como quedó.
    Ticket create(Long requesterId, CreateTicketCommand command);
}
