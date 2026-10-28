package com.riwi.supportticket.domain.model;

import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.SlaPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Prueba unitaria pura de SlaPolicy.calculateDeadline.
// Regla de negocio: fecha límite = fecha de creación + horas de la prioridad, en horas calendario 24/7
// (sin horario laboral). CRITICA 2 h, ALTA 8 h, MEDIA 24 h, BAJA 48 h.
// Se usa una fecha fija (27/10/2026 08:00) para que el resultado siempre sea el mismo.
class SlaPolicyTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 27, 8, 0);

    // Entrada: CRITICA, creado 27/10 08:00. Esperado: vence 27/10 10:00 (+2 h).
    @Test
    @DisplayName("CRITICA vence 2 horas después de creado")
    void criticaHasTwoHours() {
        SlaPolicy policy = new SlaPolicy(Priority.CRITICA, 2);
        assertEquals(LocalDateTime.of(2026, 10, 27, 10, 0), policy.calculateDeadline(CREATED_AT));
    }

    // Entrada: ALTA, creado 27/10 08:00. Esperado: vence 27/10 16:00 (+8 h).
    @Test
    @DisplayName("ALTA vence 8 horas después de creado")
    void altaHasEightHours() {
        SlaPolicy policy = new SlaPolicy(Priority.ALTA, 8);
        assertEquals(LocalDateTime.of(2026, 10, 27, 16, 0), policy.calculateDeadline(CREATED_AT));
    }

    // Entrada: MEDIA, creado 27/10 08:00. Esperado: vence 28/10 08:00 (+24 h, pasa al día siguiente).
    @Test
    @DisplayName("MEDIA vence 24 horas después de creado")
    void mediaHasTwentyFourHours() {
        SlaPolicy policy = new SlaPolicy(Priority.MEDIA, 24);
        assertEquals(LocalDateTime.of(2026, 10, 28, 8, 0), policy.calculateDeadline(CREATED_AT));
    }

    // Entrada: BAJA, creado 27/10 08:00. Esperado: vence 29/10 08:00 (+48 h, dos días después).
    @Test
    @DisplayName("BAJA vence 48 horas después de creado")
    void bajaHasFortyEightHours() {
        SlaPolicy policy = new SlaPolicy(Priority.BAJA, 48);
        assertEquals(LocalDateTime.of(2026, 10, 29, 8, 0), policy.calculateDeadline(CREATED_AT));
    }
}
