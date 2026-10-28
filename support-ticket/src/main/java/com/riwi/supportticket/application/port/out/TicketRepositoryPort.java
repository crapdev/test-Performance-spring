package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;

import java.util.List;
import java.util.Optional;

public interface TicketRepositoryPort {

    // Devuelve el ticket guardado (con id si era nuevo)
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    List<Ticket> findByRequesterId(Long requesterId);

    List<Ticket> findByTechnicianId(Long technicianId);

    List<Ticket> findAll();

    // Tickets vivos: ABIERTO, ASIGNADO o EN_PROGRESO (los que revisa el escalamiento)
    List<Ticket> findAlive();

    TicketHistory saveHistory(TicketHistory history);

    List<TicketHistory> findHistoryByTicketId(Long ticketId);
}
