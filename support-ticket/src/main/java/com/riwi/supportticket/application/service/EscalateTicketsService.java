package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.ticket.EscalateTicketsUseCase;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

// Escala una sola vez los tickets que ya gastaron el porcentaje de SLA (75 % por defecto)
// y cuenta los vencidos para la métrica tickets.sla.overdue
public class EscalateTicketsService implements EscalateTicketsUseCase {

    private static final Logger log = LoggerFactory.getLogger(EscalateTicketsService.class);

    private final TicketRepositoryPort ticketRepository;
    private final TicketMetricsPort metrics;
    private final Clock clock;
    private final int escalationPercentage;

    public EscalateTicketsService(TicketRepositoryPort ticketRepository, TicketMetricsPort metrics, Clock clock,
                                  int escalationPercentage) {
        this.ticketRepository = ticketRepository;
        this.metrics = metrics;
        this.clock = clock;
        this.escalationPercentage = escalationPercentage;
    }

    @Override
    @Transactional
    public int escalate() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Ticket> aliveTickets = ticketRepository.findAlive();
        int overdueCount = 0;
        int escalatedCount = 0;

        for (Ticket ticket : aliveTickets) {
            if (ticket.isOverdue(now)) {
                overdueCount++;
            }
            if (!ticket.isEscalated() && ticket.isNearBreach(now, escalationPercentage)) {
                Priority oldPriority = ticket.getPriority();
                ticket.escalate();
                ticketRepository.save(ticket);
                // Escalar no cambia el estado: from y to quedan iguales
                ticketRepository.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.ESCALATED,
                        ticket.getStatus(), ticket.getStatus(),
                        "Priority " + oldPriority + " -> " + ticket.getPriority(), null, now));
                log.info("Ticket {} escalated from {} to {}", ticket.getId(), oldPriority, ticket.getPriority());
                escalatedCount++;
            }
        }

        metrics.overdueTickets(overdueCount);
        return escalatedCount;
    }
}
