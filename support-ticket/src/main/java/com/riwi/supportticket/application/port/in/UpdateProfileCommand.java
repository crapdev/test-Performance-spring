package com.riwi.supportticket.application.port.in;

// Perfil básico: lo único que el usuario puede editar de sí mismo
public record UpdateProfileCommand(
        String fullName,
        String sede,
        String area
) {
}
