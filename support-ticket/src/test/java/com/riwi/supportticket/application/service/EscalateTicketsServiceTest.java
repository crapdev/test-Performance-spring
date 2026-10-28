package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Prueba unitaria de EscalateTicketsService con Mockito.
// Escalamiento al 75 % del SLA, una sola vez (ANALISIS §6). "Ahora" es fijo para que los porcentajes siempre den igual.
@ExtendWith(MockitoExtension.class)
class EscalateTicketsServiceTest {

    // "Ahora" fijo: 2026-10-27 18:00
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 18, 0);

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TicketMetricsPort metrics;

    private EscalateTicketsService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new EscalateTicketsService(ticketRepository, metrics, clock, 75);
    }

    // Ticket ALTA (8 h de SLA) creado "hoursAgo" horas antes de NOW
    private Ticket aliveTicket(double hoursAgo) {
        LocalDateTime createdAt = NOW.minusMinutes((long) (hoursAgo * 60));
        Ticket ticket = new Ticket(1L, "Sin internet", "No carga", Category.RED, Priority.ALTA,
                "Barranquilla", "Portátil", createdAt, createdAt.plusHours(8));
        ticket.setId(125L);
        return ticket;
    }

    // Entrada: ticket ALTA (8 h de SLA) creado hace 6.4 h → gastó el 80 % del tiempo.
    // Esperado: se escala 1 ticket: pasa de ALTA a CRITICA, queda escalated = true y con historial ESCALATED.
    @Test
    @DisplayName("Ticket al 80 % del SLA escala: sube un nivel y deja historial")
    void ticketAt80PercentEscalates() {
        Ticket ticket = aliveTicket(6.4); // 6.4 de 8 h = 80 %
        when(ticketRepository.findAlive()).thenReturn(List.of(ticket));

        int escalated = service.escalate();

        assertEquals(1, escalated);
        assertEquals(Priority.CRITICA, ticket.getPriority());
        assertTrue(ticket.isEscalated());
        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(ticketRepository).saveHistory(captor.capture());
        assertEquals(HistoryAction.ESCALATED, captor.getValue().getAction());
    }

    // Entrada: ticket ALTA creado hace 4 h → gastó el 50 % (todavía no llega al 75 %).
    // Esperado: no se escala nada: sigue en ALTA y no se guarda.
    @Test
    @DisplayName("Ticket al 50 % del SLA no escala")
    void ticketAt50PercentDoesNotEscalate() {
        Ticket ticket = aliveTicket(4); // 4 de 8 h = 50 %
        when(ticketRepository.findAlive()).thenReturn(List.of(ticket));

        int escalated = service.escalate();

        assertEquals(0, escalated);
        assertEquals(Priority.ALTA, ticket.getPriority());
        verify(ticketRepository, never()).save(any());
    }

    // Entrada: ticket al 87 % que ya tiene escalated = true.
    // Esperado: no se vuelve a escalar (sigue en ALTA y sin historial nuevo): se escala una sola vez.
    @Test
    @DisplayName("Ticket ya escalado no se vuelve a escalar")
    void alreadyEscalatedTicketIsNotEscalatedAgain() {
        Ticket ticket = aliveTicket(7);
        ticket.setEscalated(true);
        when(ticketRepository.findAlive()).thenReturn(List.of(ticket));

        int escalated = service.escalate();

        assertEquals(0, escalated);
        assertEquals(Priority.ALTA, ticket.getPriority());
        verify(ticketRepository, never()).saveHistory(any());
    }

    // Entrada: un ticket creado hace 10 h (vencido, su SLA era de 8 h) y otro creado hace 1 h.
    // Esperado: la métrica tickets.sla.overdue recibe 1.
    @Test
    @DisplayName("Cuenta los tickets vencidos para la métrica")
    void countsOverdueTickets() {
        Ticket overdue = aliveTicket(10); // pasó las 8 h
        Ticket onTime = aliveTicket(1);
        when(ticketRepository.findAlive()).thenReturn(List.of(overdue, onTime));

        service.escalate();

        verify(metrics).overdueTickets(1);
    }
}
