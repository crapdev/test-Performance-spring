package com.riwi.supportticket.domain.model.ticket;

import java.time.LocalDateTime;

// Horas máximas de resolución por prioridad (vienen de la tabla sla_policies)
public class SlaPolicy {

    private Priority priority;    // Prioridad a la que aplica (una fila por prioridad)
    private int resolutionHours; // Horas máximas: CRITICA 2, ALTA 8, MEDIA 24, BAJA 48

    public SlaPolicy() {
    }

    public SlaPolicy(Priority priority, int resolutionHours) {
        this.priority = priority;
        this.resolutionHours = resolutionHours;
    }

    // Fecha límite = fecha de creación + horas de la prioridad (horas calendario 24/7)
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        return createdAt.plusHours(resolutionHours);
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(int resolutionHours) {
        this.resolutionHours = resolutionHours;
    }
}
