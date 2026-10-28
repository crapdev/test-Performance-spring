package com.riwi.supportticket.infrastructure.adapter.out.dispatch;

// Body que espera technician-dispatch-service (nombres tal cual el PDF)
public record DispatchRequestDto(
        Long ticketId,
        String categoria,
        String prioridad
) {
}
