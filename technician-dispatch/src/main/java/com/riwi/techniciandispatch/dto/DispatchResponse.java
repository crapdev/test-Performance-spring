package com.riwi.techniciandispatch.dto;

// Técnico sugerido y su disponibilidad simulada
public record DispatchResponse(
        Long ticketId,
        String tecnicoId,
        String disponibilidad,
        int tiempoEstimadoHoras
) {
}
