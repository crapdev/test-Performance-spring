package com.riwi.supportticket.domain.model.ticket;

import java.time.LocalDateTime;

// Una fila del historial del ticket: qué pasó, quién lo hizo y cuándo
public class TicketHistory {

    private Long id;
    private Long ticketId;             // A qué ticket pertenece (un ticket tiene muchos historiales)
    private HistoryAction action;      // CREATED, ASSIGNED, NOT_ASSIGNED, STATUS_CHANGED o ESCALATED
    private TicketStatus fromStatus;   // Estado antes del cambio (null en CREATED)
    private TicketStatus toStatus;     // Estado después del cambio
    private String observation;        // Obligatoria en RESUELTO y CANCELADO; en NOT_ASSIGNED guarda el motivo
    private Long performedBy;          // Responsable (id del usuario); null cuando lo hace el sistema
    private LocalDateTime performedAt; // Cuándo pasó

    public TicketHistory() {
    }

    public TicketHistory(Long ticketId, HistoryAction action, TicketStatus fromStatus, TicketStatus toStatus,
                         String observation, Long performedBy, LocalDateTime performedAt) {
        this.ticketId = ticketId;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.observation = observation;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public HistoryAction getAction() {
        return action;
    }

    public void setAction(HistoryAction action) {
        this.action = action;
    }

    public TicketStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(TicketStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public TicketStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(TicketStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Long performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}
