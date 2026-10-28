package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.ticket.CreateTicketCommand;
import com.riwi.supportticket.application.port.out.SlaPolicyRepositoryPort;
import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.exception.user.InactiveUserException;
import com.riwi.supportticket.domain.model.dispatch.DispatchSuggestion;
import com.riwi.supportticket.domain.model.technician.Specialty;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.SlaPolicy;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.domain.model.user.Role;
import com.riwi.supportticket.domain.model.user.User;

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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Prueba unitaria de CreateTicketService con Mockito (sin Spring ni base de datos).
// Reglas de creación y asignación (ANALISIS §7). El dispatch siempre es un mock, así que se controla qué técnico sugiere.
// La hora es fija (27/10/2026 14:00 UTC) para que las fechas siempre den lo mismo.
@ExtendWith(MockitoExtension.class)
class CreateTicketServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long TICKET_ID = 125L;

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TechnicianRepositoryPort technicianRepository;
    @Mock
    private SlaPolicyRepositoryPort slaPolicyRepository;
    @Mock
    private TechnicianDispatchPort dispatchPort;
    @Mock
    private TicketMetricsPort metrics;

    private CreateTicketService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:00:00Z"), ZoneOffset.UTC);
        service = new CreateTicketService(userRepository, ticketRepository, technicianRepository,
                slaPolicyRepository, dispatchPort, metrics, clock);
    }

    private User user(boolean active) {
        return new User(USER_ID, "1001", "Ana Pérez", "ana@helpflow.com", "Barranquilla", "Contabilidad",
                active, Role.USUARIO, "hash");
    }

    private Technician technician(Specialty specialty, String sede) {
        return new Technician(14L, 50L, "TEC-014", specialty, sede, 3,
                new BigDecimal("3500000"), LocalDate.of(2024, 1, 15), true);
    }

    private CreateTicketCommand command(Category category) {
        return new CreateTicketCommand("Sin internet", "No carga ninguna página", category, Priority.ALTA,
                "Barranquilla", "Portátil HP-0231");
    }

    // Prepara usuario activo, política de 8 h y un save que le pone id al ticket
    private void givenActiveUserAndPolicy() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(true)));
        when(slaPolicyRepository.findByPriority(Priority.ALTA)).thenReturn(Optional.of(new SlaPolicy(Priority.ALTA, 8)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(TICKET_ID);
            return ticket;
        });
    }

    private HistoryAction lastHistoryAction() {
        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(ticketRepository, times(2)).saveHistory(captor.capture());
        List<TicketHistory> saved = captor.getAllValues();
        return saved.get(saved.size() - 1).getAction();
    }

    // Entrada: el usuario que crea el ticket está inactivo (active = false).
    // Esperado: InactiveUserException (en la API, 422) y el ticket ni siquiera se guarda.
    @Test
    @DisplayName("Usuario inactivo no puede crear tickets")
    void inactiveUserThrows() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(false)));

        assertThrows(InactiveUserException.class, () -> service.create(USER_ID, command(Category.RED)));
        verify(ticketRepository, never()).save(any());
    }

    // Entrada: ticket RED/ALTA; el dispatch sugiere TEC-014 (RED, activo, 1 ticket activo de 3, 3 h estimadas).
    // Esperado: el ticket queda ASIGNADO a TEC-014, el último historial es ASSIGNED, el dispatch se llamó una sola vez
    // y se contó la métrica tickets.created.
    @Test
    @DisplayName("Dispatch sugiere un técnico válido → ASIGNADO")
    void validSuggestionAssignsTicket() {
        givenActiveUserAndPolicy();
        when(dispatchPort.suggest(TICKET_ID, Category.RED, Priority.ALTA))
                .thenReturn(new DispatchSuggestion(TICKET_ID, "TEC-014", "MEDIA", 3));
        when(technicianRepository.findByCode("TEC-014")).thenReturn(Optional.of(technician(Specialty.RED, "Bogotá")));
        when(technicianRepository.countActiveTickets(14L)).thenReturn(1L);

        Ticket ticket = service.create(USER_ID, command(Category.RED));

        assertEquals(TicketStatus.ASIGNADO, ticket.getStatus());
        assertEquals(14L, ticket.getTechnicianId());
        assertEquals(HistoryAction.ASSIGNED, lastHistoryAction());
        verify(dispatchPort, times(1)).suggest(TICKET_ID, Category.RED, Priority.ALTA);
        verify(metrics).ticketCreated(Category.RED, Priority.ALTA);
    }

    // Entrada: ticket HARDWARE en Barranquilla; el técnico sugerido es de Medellín.
    // Esperado: queda ABIERTO, sin técnico, con historial NOT_ASSIGNED (HARDWARE exige ir en persona: misma sede).
    @Test
    @DisplayName("HARDWARE con técnico de otra sede → queda ABIERTO")
    void hardwareTechnicianFromOtherSedeLeavesOpen() {
        givenActiveUserAndPolicy();
        when(dispatchPort.suggest(TICKET_ID, Category.HARDWARE, Priority.ALTA))
                .thenReturn(new DispatchSuggestion(TICKET_ID, "TEC-014", "MEDIA", 3));
        when(technicianRepository.findByCode("TEC-014"))
                .thenReturn(Optional.of(technician(Specialty.HARDWARE, "Medellín")));
        when(technicianRepository.countActiveTickets(14L)).thenReturn(0L);

        Ticket ticket = service.create(USER_ID, command(Category.HARDWARE));

        assertEquals(TicketStatus.ABIERTO, ticket.getStatus());
        assertNull(ticket.getTechnicianId());
        assertEquals(HistoryAction.NOT_ASSIGNED, lastHistoryAction());
    }

    // Entrada: el técnico sugerido ya tiene 3 tickets activos (su máximo).
    // Esperado: queda ABIERTO con historial NOT_ASSIGNED (regla de carga máxima).
    @Test
    @DisplayName("Técnico con la carga máxima → queda ABIERTO")
    void technicianAtMaxLoadLeavesOpen() {
        givenActiveUserAndPolicy();
        when(dispatchPort.suggest(TICKET_ID, Category.RED, Priority.ALTA))
                .thenReturn(new DispatchSuggestion(TICKET_ID, "TEC-014", "MEDIA", 3));
        when(technicianRepository.findByCode("TEC-014")).thenReturn(Optional.of(technician(Specialty.RED, "Bogotá")));
        when(technicianRepository.countActiveTickets(14L)).thenReturn(3L);

        Ticket ticket = service.create(USER_ID, command(Category.RED));

        assertEquals(TicketStatus.ABIERTO, ticket.getStatus());
        assertEquals(HistoryAction.NOT_ASSIGNED, lastHistoryAction());
    }

    // Entrada: ticket ALTA (8 h de SLA); el dispatch dice que tardará 9 h.
    // Esperado: queda ABIERTO (ahora + 9 h pasa la fecha límite del SLA).
    @Test
    @DisplayName("Tiempo estimado pasa la fecha límite del SLA → queda ABIERTO")
    void estimatedTimeAfterDeadlineLeavesOpen() {
        givenActiveUserAndPolicy();
        // ALTA = 8 h de SLA; el dispatch dice 9 h
        when(dispatchPort.suggest(TICKET_ID, Category.RED, Priority.ALTA))
                .thenReturn(new DispatchSuggestion(TICKET_ID, "TEC-014", "BAJA", 9));
        when(technicianRepository.findByCode("TEC-014")).thenReturn(Optional.of(technician(Specialty.RED, "Bogotá")));
        when(technicianRepository.countActiveTickets(14L)).thenReturn(0L);

        Ticket ticket = service.create(USER_ID, command(Category.RED));

        assertEquals(TicketStatus.ABIERTO, ticket.getStatus());
    }

    // Entrada: el dispatch lanza una excepción (servicio caído).
    // Esperado: el ticket no se pierde: queda ABIERTO con historial NOT_ASSIGNED y no se busca ningún técnico.
    @Test
    @DisplayName("Dispatch lanza excepción → queda ABIERTO con NOT_ASSIGNED")
    void dispatchFailureLeavesOpen() {
        givenActiveUserAndPolicy();
        when(dispatchPort.suggest(TICKET_ID, Category.RED, Priority.ALTA))
                .thenThrow(new RuntimeException("Connection refused"));

        Ticket ticket = service.create(USER_ID, command(Category.RED));

        assertEquals(TicketStatus.ABIERTO, ticket.getStatus());
        assertEquals(HistoryAction.NOT_ASSIGNED, lastHistoryAction());
        verify(technicianRepository, never()).findByCode(any());
    }

    // Entrada: ticket ALTA (política de 8 h).
    // Esperado: slaDeadline = createdAt + 8 horas.
    @Test
    @DisplayName("La fecha límite es creación + horas de la prioridad")
    void deadlineUsesSlaPolicy() {
        givenActiveUserAndPolicy();
        when(dispatchPort.suggest(TICKET_ID, Category.RED, Priority.ALTA))
                .thenThrow(new RuntimeException("Connection refused"));

        Ticket ticket = service.create(USER_ID, command(Category.RED));

        assertTrue(ticket.getSlaDeadline().equals(ticket.getCreatedAt().plusHours(8)));
    }
}
