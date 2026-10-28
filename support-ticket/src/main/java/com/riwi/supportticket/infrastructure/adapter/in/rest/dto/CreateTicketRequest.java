package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank String description,
        @NotNull Category category,
        @NotNull Priority priority,
        @NotBlank @Size(max = 60) String sede,
        @NotBlank @Size(max = 120) String affectedAsset
) {
}
