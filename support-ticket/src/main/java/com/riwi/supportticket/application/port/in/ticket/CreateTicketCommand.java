package com.riwi.supportticket.application.port.in.ticket;

import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;

public record CreateTicketCommand(
        String title,
        String description,
        Category category,
        Priority priority,
        String sede,
        String affectedAsset
) {
}
