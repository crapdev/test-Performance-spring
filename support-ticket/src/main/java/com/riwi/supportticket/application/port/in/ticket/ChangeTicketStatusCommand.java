package com.riwi.supportticket.application.port.in.ticket;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;

// Observación obligatoria al pasar a RESUELTO o CANCELADO
public record ChangeTicketStatusCommand(
        TicketStatus newStatus,
        String observation
) {
}
