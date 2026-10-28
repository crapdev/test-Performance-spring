package com.riwi.supportticket.domain.model.ticket;

// Estados del ticket. Las transiciones válidas viven aquí mismo.
public enum TicketStatus {
    ABIERTO,
    ASIGNADO,
    EN_PROGRESO,
    RESUELTO,
    CERRADO,
    CANCELADO;

    public boolean canTransitionTo(TicketStatus next) {
        switch (this) {
            case ABIERTO:
                return next == ASIGNADO || next == CANCELADO;
            case ASIGNADO:
                return next == EN_PROGRESO || next == CANCELADO;
            case EN_PROGRESO:
                return next == RESUELTO; // ya no se puede cancelar
            case RESUELTO:
                return next == CERRADO;
            default:
                return false; // CERRADO y CANCELADO son finales
        }
    }

    // Ticket vivo: todavía cuenta para el SLA
    public boolean isAlive() {
        return this == ABIERTO || this == ASIGNADO || this == EN_PROGRESO;
    }
}
