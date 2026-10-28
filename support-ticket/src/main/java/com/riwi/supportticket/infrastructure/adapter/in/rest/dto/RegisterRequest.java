package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// No trae rol: el registro siempre crea ROLE_USUARIO
public record RegisterRequest(
        @NotBlank @Pattern(regexp = "\\d{6,15}", message = "must contain between 6 and 15 digits")
        String document,
        @NotBlank @Size(max = 120)
        String fullName,
        @NotBlank @Email @Size(max = 120)
        @Pattern(regexp = ".*@helpflow\\.com$", message = "must be a corporate email (@helpflow.com)")
        String email,
        @NotBlank @Size(max = 60)
        String sede,
        @NotBlank @Size(max = 60)
        String area,
        @NotBlank @Size(min = 6, max = 72)
        String password
) {
}
