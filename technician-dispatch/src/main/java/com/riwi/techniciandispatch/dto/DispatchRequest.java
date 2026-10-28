package com.riwi.techniciandispatch.dto;

import jakarta.validation.constraints.NotNull;

// Datos que envía support-ticket-service para pedir un técnico
public record DispatchRequest(
        @NotNull Long ticketId,
        @NotNull Categoria categoria,
        @NotNull Prioridad prioridad
) {
}
