package com.riwi.supportticket.domain.model;

import com.riwi.supportticket.domain.model.technician.Specialty;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.Category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba unitaria pura de Technician.canHandle e isActive.
// Reglas de negocio que se validan al asignar un ticket (ANALISIS §7, paso 5):
//  - "Compatibilidad categoría–especialidad": el técnico atiende su especialidad; OTRO lo atiende cualquiera.
//  - "Técnico habilitado": solo un técnico activo recibe asignaciones.
class TechnicianTest {

    // Arma un técnico de prueba. Solo cambian la especialidad y el estado; el resto da igual para estas reglas.
    private Technician technician(Specialty specialty, boolean active) {
        return new Technician(1L, 10L, "TEC-014", specialty, "Barranquilla", 3,
                new BigDecimal("3500000"), LocalDate.of(2024, 1, 15), active);
    }

    // Entrada: técnico de RED, ticket de RED. Esperado: true (sí lo puede atender).
    @Test
    @DisplayName("Atiende la categoría igual a su especialidad")
    void handlesCategoryEqualToSpecialty() {
        assertTrue(technician(Specialty.RED, true).canHandle(Category.RED));
    }

    // Entrada: técnico de RED, ticket de HARDWARE. Esperado: false (en la creación el ticket queda ABIERTO
    // con historial NOT_ASSIGNED).
    @Test
    @DisplayName("No atiende una categoría distinta a su especialidad")
    void doesNotHandleOtherCategory() {
        assertFalse(technician(Specialty.RED, true).canHandle(Category.HARDWARE));
    }

    // Entrada: un técnico de cada especialidad, ticket OTRO. Esperado: true para los 4.
    // OTRO no existe como especialidad, así que cualquiera lo puede atender.
    @Test
    @DisplayName("Cualquier técnico atiende la categoría OTRO")
    void anyTechnicianHandlesOtro() {
        for (Specialty specialty : Specialty.values()) {
            assertTrue(technician(specialty, true).canHandle(Category.OTRO));
        }
    }

    // Entrada: un técnico activo y uno inactivo (como TEC-020 del seed).
    // Esperado: el activo da true, el inactivo da false (no puede recibir tickets).
    @Test
    @DisplayName("Solo el técnico activo está habilitado")
    void inactiveTechnicianIsNotActive() {
        assertTrue(technician(Specialty.RED, true).isActive());
        assertFalse(technician(Specialty.RED, false).isActive());
    }
}
