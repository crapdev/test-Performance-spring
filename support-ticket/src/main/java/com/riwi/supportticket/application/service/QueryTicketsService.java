package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.QueryTicketsUseCase;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.exception.ticket.TicketAccessDeniedException;
import com.riwi.supportticket.domain.exception.ticket.TicketNotFoundException;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.domain.model.user.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueryTicketsService implements QueryTicketsUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;

    public QueryTicketsService(TicketRepositoryPort ticketRepository, TechnicianRepositoryPort technicianRepository) {
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
    }

    @Override
    public List<Ticket> findAll(Actor actor) {
        if (actor.getRole() == Role.ADMIN) {
            return ticketRepository.findAll();
        }
        if (actor.getRole() == Role.TECNICO) {
            Optional<Technician> technician = technicianRepository.findByUserId(actor.getUserId());
            if (technician.isEmpty()) {
                return new ArrayList<>();
            }
            return ticketRepository.findByTechnicianId(technician.get().getId());
        }
        return ticketRepository.findByRequesterId(actor.getUserId());
    }

    @Override
    public Ticket findById(Actor actor, Long ticketId) {
        Optional<Ticket> found = ticketRepository.findById(ticketId);
        if (found.isEmpty()) {
            throw new TicketNotFoundException(ticketId);
        }
        Ticket ticket = found.get();
        if (!canView(actor, ticket)) {
            throw new TicketAccessDeniedException(ticketId);
        }
        return ticket;
    }

    @Override
    public List<TicketHistory> findHistory(Actor actor, Long ticketId) {
        // Reutiliza findById para validar que existe y que el actor lo puede ver
        findById(actor, ticketId);
        return ticketRepository.findHistoryByTicketId(ticketId);
    }

    // Dueño, técnico asignado o admin
    private boolean canView(Actor actor, Ticket ticket) {
        if (actor.getRole() == Role.ADMIN) {
            return true;
        }
        if (actor.getRole() == Role.TECNICO) {
            Optional<Technician> technician = technicianRepository.findByUserId(actor.getUserId());
            if (technician.isEmpty()) {
                return false;
            }
            return technician.get().getId().equals(ticket.getTechnicianId());
        }
        return ticket.getRequesterId().equals(actor.getUserId());
    }
}
