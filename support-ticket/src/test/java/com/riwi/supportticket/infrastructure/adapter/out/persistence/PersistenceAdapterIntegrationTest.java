package com.riwi.supportticket.infrastructure.adapter.out.persistence;

import com.riwi.supportticket.TestcontainersConfiguration;
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
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.TechnicianPersistenceMapperImpl;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.TicketPersistenceMapperImpl;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// PostgreSQL real (Testcontainers) con las migraciones V1–V3 y ddl-auto=validate.
// Sin transacción de prueba (NOT_SUPPORTED): cada llamada al adaptador corre como en la app real.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TestcontainersConfiguration.class, UserRepositoryAdapter.class, TechnicianRepositoryAdapter.class,
        TicketRepositoryAdapter.class, SlaPolicyRepositoryAdapter.class, UserPersistenceMapperImpl.class,
        TechnicianPersistenceMapperImpl.class, TicketPersistenceMapperImpl.class})
class PersistenceAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userAdapter;
    @Autowired
    private TechnicianRepositoryAdapter technicianAdapter;
    @Autowired
    private TicketRepositoryAdapter ticketAdapter;
    @Autowired
    private SlaPolicyRepositoryAdapter slaPolicyAdapter;

    // Entrada: la base recién creada por Flyway (V1–V3).
    // Esperado: existe el admin con rol ADMIN, TEC-014 es de RED y tiene usuario, TEC-020 está inactivo,
    // CRITICA tiene 2 h de SLA y BAJA 48 h.
    @Test
    @DisplayName("V3 siembra admin, técnicos y las 4 políticas de SLA")
    void seedIsLoaded() {
        Optional<User> admin = userAdapter.findByEmail("admin@helpflow.com");
        assertTrue(admin.isPresent());
        assertEquals(Role.ADMIN, admin.get().getRole());

        Technician tec014 = technicianAdapter.findByCode("TEC-014").get();
        assertEquals(Specialty.RED, tec014.getSpecialty());
        assertNotNull(tec014.getUserId());
        assertFalse(technicianAdapter.findByCode("TEC-020").get().isActive());

        Optional<SlaPolicy> critica = slaPolicyAdapter.findByPriority(Priority.CRITICA);
        assertEquals(2, critica.get().getResolutionHours());
        assertEquals(48, slaPolicyAdapter.findByPriority(Priority.BAJA).get().getResolutionHours());
    }

    // Entrada: se guarda un usuario nuevo y luego se le cambia el perfil.
    // Esperado: recibe id, el documento y el correo quedan registrados, el nombre se actualiza
    // y la contraseña no se pierde al editar.
    @Test
    @DisplayName("Registra un usuario y luego actualiza su perfil")
    void savesAndUpdatesUser() {
        User saved = userAdapter.save(new User(null, "9000000001", "Pedro Prueba", "pedro@helpflow.com",
                "Medellín", "Compras", true, Role.USUARIO, "hash"));
        assertNotNull(saved.getId());
        assertTrue(userAdapter.existsByDocument("9000000001"));
        assertTrue(userAdapter.existsByEmail("pedro@helpflow.com"));

        saved.updateProfile("Pedro Editado", "Bogotá", "Finanzas");
        userAdapter.save(saved);

        User reloaded = userAdapter.findById(saved.getId()).get();
        assertEquals("Pedro Editado", reloaded.getFullName());
        assertEquals("hash", reloaded.getPassword());
    }

    // Entrada: Ana crea un ticket, se guarda CREATED, se asigna a TEC-013 y se guarda ASSIGNED.
    // Esperado: al releerlo está ASIGNADO a TEC-013 y es de Ana; el historial trae CREATED y ASSIGNED en orden;
    // TEC-013 tiene 1 ticket activo y el ticket aparece en las consultas por técnico, por solicitante y de vivos.
    @Test
    @DisplayName("Guarda un ticket, lo asigna y registra su historial")
    void savesTicketWithAssignmentAndHistory() {
        User ana = userAdapter.findByEmail("ana@helpflow.com").get();
        Technician tec013 = technicianAdapter.findByCode("TEC-013").get();
        LocalDateTime now = LocalDateTime.of(2026, 10, 27, 14, 0);

        Ticket ticket = ticketAdapter.save(new Ticket(ana.getId(), "Sin internet", "No carga", Category.RED,
                Priority.ALTA, "Barranquilla", "Portátil HP-0231", now, now.plusHours(8)));
        assertNotNull(ticket.getId());
        ticketAdapter.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.CREATED, null,
                TicketStatus.ABIERTO, null, ana.getId(), now));

        ticket.assignTo(tec013.getId(), now);
        ticketAdapter.save(ticket);
        ticketAdapter.saveHistory(new TicketHistory(ticket.getId(), HistoryAction.ASSIGNED, TicketStatus.ABIERTO,
                TicketStatus.ASIGNADO, "Assigned to TEC-013", null, now));

        Ticket reloaded = ticketAdapter.findById(ticket.getId()).get();
        assertEquals(TicketStatus.ASIGNADO, reloaded.getStatus());
        assertEquals(tec013.getId(), reloaded.getTechnicianId());
        assertEquals(ana.getId(), reloaded.getRequesterId());

        List<TicketHistory> history = ticketAdapter.findHistoryByTicketId(ticket.getId());
        assertEquals(2, history.size());
        assertEquals(HistoryAction.CREATED, history.get(0).getAction());
        assertEquals(HistoryAction.ASSIGNED, history.get(1).getAction());

        assertEquals(1, technicianAdapter.countActiveTickets(tec013.getId()));
        assertEquals(1, ticketAdapter.findByTechnicianId(tec013.getId()).size());
        assertFalse(ticketAdapter.findByRequesterId(ana.getId()).isEmpty());
        assertFalse(ticketAdapter.findAlive().isEmpty());
    }
}
