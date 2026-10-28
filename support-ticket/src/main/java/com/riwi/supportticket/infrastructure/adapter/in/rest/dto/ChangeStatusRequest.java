package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.infrastructure.adapter.in.rest.validation.ValidStatusChange;
import jakarta.validation.constraints.NotNull;

// Validación cruzada: RESUELTO y CANCELADO exigen observación
@ValidStatusChange
public record ChangeStatusRequest(
        @NotNull TicketStatus newStatus,
        String observation
) {
}
