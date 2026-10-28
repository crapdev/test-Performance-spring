package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusCommand;
import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusUseCase;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.exception.ObservationRequiredException;
import com.riwi.supportticket.domain.exception.ticket.TicketAccessDeniedException;
import com.riwi.supportticket.domain.exception.ticket.TicketNotFoundException;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.domain.model.user.Role;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

// Quién puede pedir cada estado (ANALISIS §5):
//  - TECNICO: EN_PROGRESO y RESUELTO, solo en sus tickets asignados
//  - USUARIO: CERRADO y CANCELADO, solo en sus tickets
//  - ADMIN: no cambia estados
public class ChangeTicketStatusService implements ChangeTicketStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChangeTicketStatusService.class);

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public ChangeTicketStatusService(TicketRepositoryPort ticketRepository,
                                     TechnicianRepositoryPort technicianRepository,
                                     TicketMetricsPort metrics, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Ticket changeStatus(Actor actor, Long ticketId, ChangeTicketStatusCommand command) {
        Optional<Ticket> found = ticketRepository.findById(ticketId);
        if (found.isEmpty()) {
            throw new TicketNotFoundException(ticketId);
        }
        Ticket ticket = found.get();
        TicketStatus newStatus = command.newStatus();

        // 1. Permiso (403), 2. observación (422), 3. transición válida (409, la valida el Ticket)
        checkPermission(actor, ticket, newStatus);
        if (requiresObservation(newStatus) && isBlank(command.observation())) {
            throw new ObservationRequiredException(newStatus);
        }

        TicketStatus fromStatus = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now(clock);
        ticket.changeStatus(newStatus, now);
        ticket = ticketRepository.save(ticket);
        ticketRepository.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.STATUS_CHANGED,
                fromStatus, newStatus, command.observation(), actor.getUserId(), now));
        log.info("Ticket {} moved from {} to {} by user {} ({})",
                ticket.getId(), fromStatus, newStatus, actor.getUserId(), actor.getRole());

        if (newStatus == TicketStatus.EN_PROGRESO) {
            metrics.firstResponse(Duration.between(ticket.getCreatedAt(), now));
        }
        if (newStatus == TicketStatus.RESUELTO) {
            metrics.resolution(Duration.between(ticket.getCreatedAt(), now));
        }
        return ticket;
    }

    private void checkPermission(Actor actor, Ticket ticket, TicketStatus newStatus) {
        if (actor.getRole() == Role.TECNICO) {
            boolean allowedStatus = newStatus == TicketStatus.EN_PROGRESO || newStatus == TicketStatus.RESUELTO;
            if (!allowedStatus || !isAssignedTechnician(actor, ticket)) {
                throw new TicketAccessDeniedException(ticket.getId());
            }
        } else if (actor.getRole() == Role.USUARIO) {
            boolean allowedStatus = newStatus == TicketStatus.CERRADO || newStatus == TicketStatus.CANCELADO;
            if (!allowedStatus || !ticket.getRequesterId().equals(actor.getUserId())) {
                throw new TicketAccessDeniedException(ticket.getId());
            }
        } else {
            throw new TicketAccessDeniedException(ticket.getId());
        }
    }

    // El JWT trae el userId; el ticket guarda el id del técnico
    private boolean isAssignedTechnician(Actor actor, Ticket ticket) {
        Optional<Technician> technician = technicianRepository.findByUserId(actor.getUserId());
        if (technician.isEmpty()) {
            return false;
        }
        return technician.get().getId().equals(ticket.getTechnicianId());
    }

    private boolean requiresObservation(TicketStatus status) {
        return status == TicketStatus.RESUELTO || status == TicketStatus.CANCELADO;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
