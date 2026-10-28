package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

import com.riwi.supportticket.domain.model.user.Role;

// Nunca incluye la contraseña
public record UserResponse(
        Long id,
        String document,
        String fullName,
        String email,
        String sede,
        String area,
        boolean active,
        Role role
) {
}
