package com.riwi.techniciandispatch.service;

import com.riwi.techniciandispatch.dto.Categoria;
import com.riwi.techniciandispatch.dto.DispatchRequest;
import com.riwi.techniciandispatch.dto.DispatchResponse;
import com.riwi.techniciandispatch.dto.Prioridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba unitaria pura del algoritmo del dispatch (sin Spring): seed = ticketId·7 + categoria·3 + prioridad.
// Con ese número se elige el técnico (5 por categoría), las horas (según prioridad) y la disponibilidad.
class DispatchServiceTest {

    private final DispatchService service = new DispatchService();

    // Entrada: el mismo body dos veces (ticket 42, SOFTWARE, MEDIA).
    // Esperado: las dos respuestas son idénticas (mismo técnico, horas y disponibilidad). Es la regla "determinista" del PDF.
    @Test
    @DisplayName("Misma entrada dos veces → misma salida")
    void sameInputTwiceGivesSameOutput() {
        DispatchRequest request = new DispatchRequest(42L, Categoria.SOFTWARE, Prioridad.MEDIA);

        DispatchResponse first = service.dispatch(request);
        DispatchResponse second = service.dispatch(request);

        assertEquals(first, second);
    }

    // Entrada: el ejemplo del PDF (ticket 125, RED, ALTA). seed = 125·7 + 2·3 + 2 = 883.
    // Esperado: TEC-014 (2·5 + 883 % 5 + 1 = 14), 3 horas (883 % 10) y disponibilidad MEDIA.
    @Test
    @DisplayName("Ejemplo del PDF (125, RED, ALTA) → TEC-014, 3 h, MEDIA")
    void pdfExampleReturnsTec014() {
        // seed = 125*7 + 2*3 + 2 = 883
        DispatchResponse response = service.dispatch(new DispatchRequest(125L, Categoria.RED, Prioridad.ALTA));

        assertEquals(125L, response.ticketId());
        assertEquals("TEC-014", response.tecnicoId());
        assertEquals(3, response.tiempoEstimadoHoras());
        assertEquals("MEDIA", response.disponibilidad());
    }

    // Entrada: los bordes de cada rango de horas.
    // Esperado: 2 h → ALTA, 3 h → MEDIA, 8 h → MEDIA, 9 h → BAJA (0-2 ALTA, 3-8 MEDIA, más de 8 BAJA).
    @Test
    @DisplayName("Bordes de disponibilidad: 2 ALTA, 3 MEDIA, 8 MEDIA, 9 BAJA")
    void availabilityBorders() {
        assertEquals("ALTA", service.resolveAvailability(2));
        assertEquals("MEDIA", service.resolveAvailability(3));
        assertEquals("MEDIA", service.resolveAvailability(8));
        assertEquals("BAJA", service.resolveAvailability(9));
    }

    // Entrada: 50 tickets distintos de categoría RED.
    // Esperado: todos los técnicos sugeridos están entre TEC-011 y TEC-015 (el bloque de RED: 5 técnicos por categoría).
    @Test
    @DisplayName("RED siempre sugiere TEC-011 a TEC-015")
    void technicianStaysInsideCategoryBlock() {
        // RED solo puede devolver TEC-011 a TEC-015
        for (long ticketId = 1; ticketId <= 50; ticketId++) {
            DispatchResponse response = service.dispatch(new DispatchRequest(ticketId, Categoria.RED, Prioridad.BAJA));
            int number = Integer.parseInt(response.tecnicoId().substring(4));
            assertTrue(number >= 11 && number <= 15, "Fuera del bloque RED: " + response.tecnicoId());
        }
    }

    // Entrada: 50 tickets distintos con prioridad CRITICA.
    // Esperado: el tiempo estimado nunca pasa de 3 horas (CRITICA usa seed % 4, que da 0 a 3).
    @Test
    @DisplayName("CRITICA nunca pasa de 3 horas")
    void criticalPriorityNeverExceedsThreeHours() {
        for (long ticketId = 1; ticketId <= 50; ticketId++) {
            DispatchResponse response = service.dispatch(new DispatchRequest(ticketId, Categoria.HARDWARE, Prioridad.CRITICA));
            assertTrue(response.tiempoEstimadoHoras() <= 3);
        }
    }
}
