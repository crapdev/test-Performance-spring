package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusCommand;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.exception.ObservationRequiredException;
import com.riwi.supportticket.domain.exception.ticket.InvalidStatusTransitionException;
import com.riwi.supportticket.domain.exception.ticket.TicketAccessDeniedException;
import com.riwi.supportticket.domain.model.technician.Specialty;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.domain.model.user.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Prueba unitaria de ChangeTicketStatusService con Mockito.
// Quién puede pedir cada estado (ANALISIS §5) y en qué orden se valida: permiso (403), observación (422), transición (409).
// El ticket de prueba es del usuario 1 y está asignado al técnico 14 (usuario 50).
@ExtendWith(MockitoExtension.class)
class ChangeTicketStatusServiceTest {

    private static final Long TICKET_ID = 125L;
    private static final Long OWNER_ID = 1L;
    private static final Long TECHNICIAN_USER_ID = 50L;
    private static final Long TECHNICIAN_ID = 14L;

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TechnicianRepositoryPort technicianRepository;
    @Mock
    private TicketMetricsPort metrics;

    private ChangeTicketStatusService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-27T16:00:00Z"), ZoneOffset.UTC);
        service = new ChangeTicketStatusService(ticketRepository, technicianRepository, metrics, clock);
    }

    // Ticket del usuario 1, asignado al técnico 14, en el estado indicado
    private Ticket ticketIn(TicketStatus status) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 27, 14, 0);
        Ticket ticket = new Ticket(OWNER_ID, "Sin internet", "No carga", Category.RED, Priority.ALTA,
                "Barranquilla", "Portátil", createdAt, createdAt.plusHours(8));
        ticket.setId(TICKET_ID);
        ticket.setTechnicianId(TECHNICIAN_ID);
        ticket.setStatus(status);
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));
        return ticket;
    }

    private Technician technician(Long id) {
        return new Technician(id, TECHNICIAN_USER_ID, "TEC-0" + id, Specialty.RED, "Bogotá", 3,
                new BigDecimal("3500000"), LocalDate.of(2024, 1, 15), true);
    }

    private void givenSaveReturnsTicket() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // Entrada: el técnico asignado pide ASIGNADO → EN_PROGRESO.
    // Esperado: el ticket queda EN_PROGRESO con firstResponseAt, se guarda historial STATUS_CHANGED (desde ASIGNADO)
    // y se registra la métrica de primera respuesta.
    @Test
    @DisplayName("Técnico asignado pasa ASIGNADO → EN_PROGRESO y guarda la primera respuesta")
    void assignedTechnicianStartsWork() {
        ticketIn(TicketStatus.ASIGNADO);
        givenSaveReturnsTicket();
        when(technicianRepository.findByUserId(TECHNICIAN_USER_ID)).thenReturn(Optional.of(technician(TECHNICIAN_ID)));

        Ticket ticket = service.changeStatus(new Actor(TECHNICIAN_USER_ID, Role.TECNICO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.EN_PROGRESO, null));

        assertEquals(TicketStatus.EN_PROGRESO, ticket.getStatus());
        assertNotNull(ticket.getFirstResponseAt());
        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(ticketRepository).saveHistory(captor.capture());
        assertEquals(HistoryAction.STATUS_CHANGED, captor.getValue().getAction());
        assertEquals(TicketStatus.ASIGNADO, captor.getValue().getFromStatus());
        verify(metrics).firstResponse(any());
    }

    // Entrada: un técnico que NO es el asignado pide EN_PROGRESO.
    // Esperado: TicketAccessDeniedException (403) y no se guarda nada.
    @Test
    @DisplayName("Técnico no asignado → 403")
    void otherTechnicianIsDenied() {
        ticketIn(TicketStatus.ASIGNADO);
        when(technicianRepository.findByUserId(TECHNICIAN_USER_ID)).thenReturn(Optional.of(technician(99L)));

        assertThrows(TicketAccessDeniedException.class, () -> service.changeStatus(
                new Actor(TECHNICIAN_USER_ID, Role.TECNICO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.EN_PROGRESO, null)));
        verify(ticketRepository, never()).save(any());
    }

    // Entrada: el técnico asignado pide RESUELTO con observación en blanco ("  ").
    // Esperado: ObservationRequiredException (422).
    @Test
    @DisplayName("Técnico pide RESUELTO sin observación → 422")
    void resolveWithoutObservationThrows() {
        ticketIn(TicketStatus.EN_PROGRESO);
        when(technicianRepository.findByUserId(TECHNICIAN_USER_ID)).thenReturn(Optional.of(technician(TECHNICIAN_ID)));

        assertThrows(ObservationRequiredException.class, () -> service.changeStatus(
                new Actor(TECHNICIAN_USER_ID, Role.TECNICO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.RESUELTO, "  ")));
    }

    // Entrada: el técnico pide CERRADO (eso solo lo hace el usuario dueño).
    // Esperado: TicketAccessDeniedException (403).
    @Test
    @DisplayName("Técnico no puede cerrar un ticket (eso lo hace el usuario) → 403")
    void technicianCannotClose() {
        ticketIn(TicketStatus.RESUELTO);

        assertThrows(TicketAccessDeniedException.class, () -> service.changeStatus(
                new Actor(TECHNICIAN_USER_ID, Role.TECNICO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.CERRADO, null)));
    }

    // Entrada: el usuario dueño pide RESUELTO → CERRADO.
    // Esperado: el ticket queda CERRADO.
    @Test
    @DisplayName("Usuario dueño cierra un ticket RESUELTO → CERRADO")
    void ownerClosesResolvedTicket() {
        ticketIn(TicketStatus.RESUELTO);
        givenSaveReturnsTicket();

        Ticket ticket = service.changeStatus(new Actor(OWNER_ID, Role.USUARIO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.CERRADO, null));

        assertEquals(TicketStatus.CERRADO, ticket.getStatus());
    }

    // Entrada: el usuario dueño pide EN_PROGRESO → CERRADO (todavía no está resuelto).
    // Esperado: InvalidStatusTransitionException (409).
    @Test
    @DisplayName("Usuario dueño cierra un ticket EN_PROGRESO → 409")
    void ownerCannotCloseInProgressTicket() {
        ticketIn(TicketStatus.EN_PROGRESO);

        assertThrows(InvalidStatusTransitionException.class, () -> service.changeStatus(
                new Actor(OWNER_ID, Role.USUARIO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.CERRADO, null)));
    }

    // Entrada: el usuario dueño pide EN_PROGRESO → CANCELADO (ya lo están trabajando).
    // Esperado: InvalidStatusTransitionException (409).
    @Test
    @DisplayName("Usuario dueño cancela un ticket EN_PROGRESO → 409")
    void ownerCannotCancelInProgressTicket() {
        ticketIn(TicketStatus.EN_PROGRESO);

        assertThrows(InvalidStatusTransitionException.class, () -> service.changeStatus(
                new Actor(OWNER_ID, Role.USUARIO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.CANCELADO, "Ya no lo necesito")));
    }

    // Entrada: un usuario que NO es el dueño intenta cancelar el ticket.
    // Esperado: TicketAccessDeniedException (403).
    @Test
    @DisplayName("Otro usuario intenta cancelar un ticket ajeno → 403")
    void otherUserIsDenied() {
        ticketIn(TicketStatus.ABIERTO);

        assertThrows(TicketAccessDeniedException.class, () -> service.changeStatus(
                new Actor(2L, Role.USUARIO), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.CANCELADO, "No es mío")));
    }

    // Entrada: el admin pide EN_PROGRESO.
    // Esperado: TicketAccessDeniedException (403): el admin consulta pero no cambia estados.
    @Test
    @DisplayName("Admin no cambia estados → 403")
    void adminIsDenied() {
        ticketIn(TicketStatus.ASIGNADO);

        assertThrows(TicketAccessDeniedException.class, () -> service.changeStatus(
                new Actor(99L, Role.ADMIN), TICKET_ID,
                new ChangeTicketStatusCommand(TicketStatus.EN_PROGRESO, null)));
    }
}
