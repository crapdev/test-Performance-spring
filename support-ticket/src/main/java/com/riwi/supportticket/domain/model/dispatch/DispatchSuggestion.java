package com.riwi.supportticket.domain.model.dispatch;

// Lo que responde technician-dispatch-service: qué técnico sugiere y en cuánto tiempo
public class DispatchSuggestion {

    private final Long ticketId;         // "ticketId": el mismo ticket que se preguntó
    private final String technicianCode; // "tecnicoId": ej.: TEC-014; con él se busca el técnico real
    private final String availability;   // "disponibilidad": ALTA, MEDIA o BAJA (simulada); solo informativa
    private final int estimatedHours;    // "tiempoEstimadoHoras": regla "now + horas no pasa de slaDeadline"

    public DispatchSuggestion(Long ticketId, String technicianCode, String availability, int estimatedHours) {
        this.ticketId = ticketId;
        this.technicianCode = technicianCode;
        this.availability = availability;
        this.estimatedHours = estimatedHours;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public String getAvailability() {
        return availability;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }
}
