package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.ticket.CreateTicketCommand;
import com.riwi.supportticket.application.port.in.ticket.CreateTicketUseCase;
import com.riwi.supportticket.application.port.out.SlaPolicyRepositoryPort;
import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.exception.user.InactiveUserException;
import com.riwi.supportticket.domain.exception.user.UserNotFoundException;
import com.riwi.supportticket.domain.model.dispatch.DispatchSuggestion;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.SlaPolicy;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.domain.model.user.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

// Crea el ticket y lo asigna con la sugerencia del dispatch (ANALISIS §7)
public class CreateTicketService implements CreateTicketUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTicketService.class);

    private final UserRepositoryPort userRepository;
    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final SlaPolicyRepositoryPort slaPolicyRepository;
    private final TechnicianDispatchPort dispatchPort;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public CreateTicketService(UserRepositoryPort userRepository, TicketRepositoryPort ticketRepository,
                               TechnicianRepositoryPort technicianRepository,
                               SlaPolicyRepositoryPort slaPolicyRepository, TechnicianDispatchPort dispatchPort,
                               TicketMetricsPort metrics, Clock clock) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.dispatchPort = dispatchPort;
        this.metrics = metrics;
        this.clock = clock;
    }

    // Todo o nada: si algo falla a la mitad no queda un ticket sin historial
    @Override
    @Transactional
    public Ticket create(Long requesterId, CreateTicketCommand command) {
        // 1. El usuario existe y está activo
        Optional<User> foundUser = userRepository.findById(requesterId);
        if (foundUser.isEmpty()) {
            throw new UserNotFoundException(requesterId);
        }
        if (!foundUser.get().isActive()) {
            throw new InactiveUserException(requesterId);
        }

        // 2. Fecha límite según la prioridad
        Optional<SlaPolicy> foundPolicy = slaPolicyRepository.findByPriority(command.priority());
        if (foundPolicy.isEmpty()) {
            throw new IllegalStateException("No SLA policy for priority " + command.priority());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime deadline = foundPolicy.get().calculateDeadline(now);

        // 3. Se guarda ABIERTO para tener el id que necesita el dispatch
        Ticket ticket = new Ticket(requesterId, command.title(), command.description(), command.category(),
                command.priority(), command.sede(), command.affectedAsset(), now, deadline);
        ticket = ticketRepository.save(ticket);
        ticketRepository.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.CREATED, null,
                TicketStatus.ABIERTO, null, requesterId, now));
        metrics.ticketCreated(ticket.getCategory(), ticket.getPriority());
        log.info("Ticket {} created by user {} (category={}, priority={})",
                ticket.getId(), requesterId, ticket.getCategory(), ticket.getPriority());

        // 4. Se pregunta al dispatch; si no responde el ticket queda ABIERTO
        DispatchSuggestion suggestion;
        try {
            suggestion = dispatchPort.suggest(ticket.getId(), ticket.getCategory(), ticket.getPriority());
        } catch (RuntimeException e) {
            log.warn("Dispatch service unavailable for ticket {}: {}", ticket.getId(), e.getMessage());
            return leaveUnassigned(ticket, "Dispatch service unavailable", now);
        }

        // 5. Se valida la sugerencia con las reglas de asignación
        Optional<Technician> foundTechnician = technicianRepository.findByCode(suggestion.getTechnicianCode());
        if (foundTechnician.isEmpty()) {
            return leaveUnassigned(ticket, suggestion.getTechnicianCode() + " does not exist", now);
        }
        Technician technician = foundTechnician.get();
        String reason = findRejectionReason(ticket, technician, suggestion, now);
        if (reason != null) {
            return leaveUnassigned(ticket, reason, now);
        }

        // 6. Todo pasó: queda ASIGNADO
        ticket.assignTo(technician.getId(), now);
        ticket = ticketRepository.save(ticket);
        ticketRepository.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.ASSIGNED,
                TicketStatus.ABIERTO, TicketStatus.ASIGNADO,
                "Assigned to " + technician.getCode(), null, now));
        log.info("Ticket {} assigned to {}", ticket.getId(), technician.getCode());
        return ticket;
    }

    // Devuelve el motivo por el que el técnico no sirve, o null si cumple todas las reglas
    private String findRejectionReason(Ticket ticket, Technician technician, DispatchSuggestion suggestion,
                                       LocalDateTime now) {
        String code = technician.getCode();
        if (!technician.isActive()) {
            return code + " is inactive";
        }
        if (!technician.canHandle(ticket.getCategory())) {
            return code + " cannot handle category " + ticket.getCategory();
        }
        if (technicianRepository.countActiveTickets(technician.getId()) >= technician.getMaxActiveTickets()) {
            return code + " has reached the maximum of active tickets";
        }
        // HARDWARE se atiende en persona: el técnico debe ser de la misma sede
        if (ticket.getCategory() == Category.HARDWARE && !technician.getSede().equalsIgnoreCase(ticket.getSede())) {
            return code + " is not from sede " + ticket.getSede();
        }
        if (now.plusHours(suggestion.getEstimatedHours()).isAfter(ticket.getSlaDeadline())) {
            return code + " cannot attend before the SLA deadline";
        }
        return null;
    }

    // El ticket sigue ABIERTO y el motivo queda en el historial
    private Ticket leaveUnassigned(Ticket ticket, String reason, LocalDateTime now) {
        log.warn("Ticket {} stays ABIERTO: {}", ticket.getId(), reason);
        ticketRepository.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.NOT_ASSIGNED,
                TicketStatus.ABIERTO, TicketStatus.ABIERTO, reason, null, now));
        return ticket;
    }
}
