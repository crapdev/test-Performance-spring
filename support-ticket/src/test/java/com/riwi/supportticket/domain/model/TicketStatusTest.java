package com.riwi.supportticket.domain.model;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba unitaria pura (sin Spring ni base de datos) de TicketStatus.canTransitionTo.
// Regla de negocio: el ticket solo puede moverse por el ciclo de vida del ANALISIS §5:
// ABIERTO -> ASIGNADO -> EN_PROGRESO -> RESUELTO -> CERRADO, y CANCELADO desde ABIERTO o ASIGNADO.
// Por qué importa: si canTransitionTo da false, el servicio responde 409 (transición inválida).
class TicketStatusTest {

    // Qué se prueba: las 6 flechas del diagrama de estados.
    // Esperado: todas devuelven true.
    @Test
    @DisplayName("Las transiciones del ciclo de vida son válidas")
    void validTransitionsAreAllowed() {
        assertTrue(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.ASIGNADO));     // el sistema asigna
        assertTrue(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.CANCELADO));    // el dueño cancela
        assertTrue(TicketStatus.ASIGNADO.canTransitionTo(TicketStatus.EN_PROGRESO)); // el técnico empieza
        assertTrue(TicketStatus.ASIGNADO.canTransitionTo(TicketStatus.CANCELADO));   // el dueño cancela
        assertTrue(TicketStatus.EN_PROGRESO.canTransitionTo(TicketStatus.RESUELTO)); // el técnico resuelve
        assertTrue(TicketStatus.RESUELTO.canTransitionTo(TicketStatus.CERRADO));     // el dueño cierra
    }

    // Qué se prueba: saltos que NO están en el diagrama.
    // Esperado: todas devuelven false (en la API serían un 409).
    @Test
    @DisplayName("Los saltos fuera del ciclo de vida se rechazan")
    void invalidTransitionsAreRejected() {
        assertFalse(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.RESUELTO));     // nadie lo ha trabajado
        assertFalse(TicketStatus.ABIERTO.canTransitionTo(TicketStatus.CERRADO));      // no se cierra sin resolver
        assertFalse(TicketStatus.ASIGNADO.canTransitionTo(TicketStatus.CERRADO));     // no se cierra sin resolver
        assertFalse(TicketStatus.EN_PROGRESO.canTransitionTo(TicketStatus.CANCELADO)); // ya se está trabajando
        assertFalse(TicketStatus.RESUELTO.canTransitionTo(TicketStatus.CANCELADO));   // ya está resuelto
    }

    // Qué se prueba: CERRADO y CANCELADO son estados finales.
    // Esperado: desde ellos no se puede ir a ningún estado (false para los 6 valores del enum).
    @Test
    @DisplayName("CERRADO y CANCELADO son finales")
    void finalStatesCannotMove() {
        for (TicketStatus next : TicketStatus.values()) {
            assertFalse(TicketStatus.CERRADO.canTransitionTo(next));
            assertFalse(TicketStatus.CANCELADO.canTransitionTo(next));
        }
    }
}
