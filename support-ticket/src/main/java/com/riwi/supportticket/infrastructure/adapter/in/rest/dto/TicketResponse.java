package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        Long requesterId,
        Long technicianId,
        String title,
        String description,
        Category category,
        Priority priority,
        String sede,
        String affectedAsset,
        TicketStatus status,
        LocalDateTime createdAt,
        LocalDateTime slaDeadline,
        LocalDateTime firstResponseAt,
        LocalDateTime resolvedAt,
        boolean escalated
) {
}
