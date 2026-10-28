package com.riwi.supportticket.domain.model.ticket;

import com.riwi.supportticket.domain.exception.ticket.InvalidStatusTransitionException;

import java.time.Duration;
import java.time.LocalDateTime;

// Ticket de soporte. Las fechas llegan por parámetro (now) para poder probar con una fecha fija.
public class Ticket {

    private Long id;                       // Es el "ticketId" que se le manda al dispatch
    private Long requesterId;              // Dueño: solo él ve, cancela y cierra el ticket (si no → 403)
    private Long technicianId;             // null mientras esté ABIERTO; solo ese técnico lo trabaja y cuenta en su carga
    private String title;                  // Máximo 120 caracteres (se valida en el DTO)
    private String description;            // Descripción del problema
    private Category category;             // Se manda al dispatch y define qué especialidad lo atiende
    private Priority priority;             // Define las horas de SLA; sube un nivel al escalar
    private String sede;                   // En HARDWARE debe coincidir con la sede del técnico
    private String affectedAsset;          // Equipo o recurso con el problema (ej.: "Portátil HP-0231")
    private TicketStatus status;           // Solo cambia con changeStatus / assignTo
    private LocalDateTime createdAt;       // Punto de partida del SLA
    private LocalDateTime slaDeadline;     // createdAt + horas de la prioridad; no cambia al escalar
    private LocalDateTime firstResponseAt; // Cuándo pasó a EN_PROGRESO → métrica tickets.first_response.time
    private LocalDateTime resolvedAt;      // Cuándo pasó a RESUELTO → métrica tickets.resolution.time
    private boolean escalated;             // Evita que el scheduler (cada 5 min) escale más de una vez

    public Ticket() {
    }

    // Ticket nuevo: siempre nace ABIERTO, sin técnico y sin escalar
    public Ticket(Long requesterId, String title, String description, Category category, Priority priority,
                  String sede, String affectedAsset, LocalDateTime createdAt, LocalDateTime slaDeadline) {
        this.requesterId = requesterId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.sede = sede;
        this.affectedAsset = affectedAsset;
        this.status = TicketStatus.ABIERTO;
        this.createdAt = createdAt;
        this.slaDeadline = slaDeadline;
        this.escalated = false;
    }

    // Cambia el estado si la transición es válida y guarda la fecha que corresponda
    public void changeStatus(TicketStatus newStatus, LocalDateTime now) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(id, status, newStatus);
        }
        if (newStatus == TicketStatus.EN_PROGRESO) {
            firstResponseAt = now;
        }
        if (newStatus == TicketStatus.RESUELTO) {
            resolvedAt = now;
        }
        status = newStatus;
    }

    // Asigna el técnico: el ticket pasa de ABIERTO a ASIGNADO
    public void assignTo(Long technicianId, LocalDateTime now) {
        changeStatus(TicketStatus.ASIGNADO, now);
        this.technicianId = technicianId;
    }

    // Sube la prioridad un nivel, una sola vez. La fecha límite no se recalcula.
    public void escalate() {
        if (escalated) {
            return;
        }
        priority = priority.next();
        escalated = true;
    }

    // Vencido: sigue vivo y ya pasó la fecha límite
    public boolean isOverdue(LocalDateTime now) {
        return status.isAlive() && now.isAfter(slaDeadline);
    }

    // Próximo a incumplir: sigue vivo y ya gastó el porcentaje indicado (ej.: 75) de su tiempo
    public boolean isNearBreach(LocalDateTime now, int percentage) {
        if (!status.isAlive()) {
            return false;
        }
        long totalMinutes = Duration.between(createdAt, slaDeadline).toMinutes();
        long usedMinutes = Duration.between(createdAt, now).toMinutes();
        return usedMinutes >= totalMinutes * percentage / 100;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getAffectedAsset() {
        return affectedAsset;
    }

    public void setAffectedAsset(String affectedAsset) {
        this.affectedAsset = affectedAsset;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public LocalDateTime getFirstResponseAt() {
        return firstResponseAt;
    }

    public void setFirstResponseAt(LocalDateTime firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }
}
