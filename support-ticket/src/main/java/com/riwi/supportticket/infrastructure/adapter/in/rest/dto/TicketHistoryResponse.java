package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        HistoryAction action,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        String observation,
        Long performedBy,
        LocalDateTime performedAt
) {
}
