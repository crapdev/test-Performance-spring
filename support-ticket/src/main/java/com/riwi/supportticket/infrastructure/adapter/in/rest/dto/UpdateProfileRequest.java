package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 60) String sede,
        @NotBlank @Size(max = 60) String area
) {
}
