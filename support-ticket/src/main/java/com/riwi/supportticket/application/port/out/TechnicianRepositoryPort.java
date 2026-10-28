package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.technician.Technician;

import java.util.Optional;

public interface TechnicianRepositoryPort {

    // Busca por el código que devuelve el dispatch (ej.: TEC-014)
    Optional<Technician> findByCode(String code);

    // Busca el técnico de un usuario con ROLE_TECNICO (el JWT solo trae el userId)
    Optional<Technician> findByUserId(Long userId);

    // Tickets ASIGNADO + EN_PROGRESO del técnico (regla de carga máxima)
    long countActiveTickets(Long technicianId);
}
