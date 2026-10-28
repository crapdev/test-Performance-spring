package com.riwi.supportticket.infrastructure.adapter.out.dispatch;

public record DispatchResponseDto(
        Long ticketId,
        String tecnicoId,
        String disponibilidad,
        int tiempoEstimadoHoras
) {
}
