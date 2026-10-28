package com.riwi.supportticket.application.port.in;

// Datos para registrarse. No trae rol: el registro siempre crea ROLE_USUARIO.
public record RegisterUserCommand(
        String document,
        String fullName,
        String email,
        String sede,
        String area,
        String password
) {
}
