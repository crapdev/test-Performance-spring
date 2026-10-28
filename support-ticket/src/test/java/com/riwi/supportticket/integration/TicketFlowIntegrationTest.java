package com.riwi.supportticket.integration;

import com.jayway.jsonpath.JsonPath;
import com.riwi.supportticket.domain.model.dispatch.DispatchSuggestion;
import com.riwi.supportticket.domain.model.ticket.HistoryAction;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba de integración: app completa + PostgreSQL real (Testcontainers) + llamadas HTTP con MockMvc.
// Flujo completo de tickets con permisos por rol. El dispatch es un mock para controlar qué técnico sugiere.
// Cada prueba usa un técnico RED distinto para no chocar con la carga máxima (3) del seed.
class TicketFlowIntegrationTest extends IntegrationTestBase {

    private static final String NEW_TICKET = "{\"title\":\"VPN no conecta\",\"description\":\"Error de túnel\","
            + "\"category\":\"RED\",\"priority\":\"ALTA\",\"sede\":\"Barranquilla\",\"affectedAsset\":\"Portátil HP-0231\"}";

    // El dispatch (mock) sugiere este técnico con 1 h estimada: pasa todas las reglas
    private void givenDispatchSuggests(String technicianCode) {
        when(dispatchPort.suggest(any(), any(), any()))
                .thenReturn(new DispatchSuggestion(0L, technicianCode, "ALTA", 1));
    }

    // Crea un ticket como el usuario del token y devuelve su id
    private long createTicket(String token) throws Exception {
        String response = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_TICKET))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private String statusBody(String newStatus, String observation) {
        if (observation == null) {
            return "{\"newStatus\":\"" + newStatus + "\"}";
        }
        return "{\"newStatus\":\"" + newStatus + "\",\"observation\":\"" + observation + "\"}";
    }

    // Entrada: el dispatch está caído; Ana crea un ticket; luego lo consultan Ana, Carlos y el admin.
    // Esperado: 201 al crear; Ana → 200 y el ticket está ABIERTO (no se perdió); Carlos → 403; admin → 200.
    @Test
    @DisplayName("Usuario A crea ticket → 201; usuario B lo consulta → 403; admin sí lo ve")
    void ownerCreatesAndOtherUserIsDenied() throws Exception {
        when(dispatchPort.suggest(any(), any(), any())).thenThrow(new RuntimeException("dispatch down"));
        String ana = TestTokens.forRole(mockMvc, Role.USUARIO);
        String carlos = TestTokens.forEmail(mockMvc, "carlos@helpflow.com");
        String admin = TestTokens.forRole(mockMvc, Role.ADMIN);

        long ticketId = createTicket(ana);

        mockMvc.perform(get("/api/tickets/" + ticketId).header("Authorization", ana))
                .andExpect(status().isOk())
                // El dispatch falló: el ticket queda ABIERTO, no se pierde
                .andExpect(jsonPath("$.status").value("ABIERTO"));
        mockMvc.perform(get("/api/tickets/" + ticketId).header("Authorization", carlos))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/api/tickets/" + ticketId).header("Authorization", admin))
                .andExpect(status().isOk());
    }

    // Entrada: un técnico hace POST /api/tickets.
    // Esperado: 403 (solo ROLE_USUARIO crea tickets, lo frena @PreAuthorize).
    @Test
    @DisplayName("Técnico no puede crear tickets → 403")
    void technicianCannotCreateTicket() throws Exception {
        String technician = TestTokens.forRole(mockMvc, Role.TECNICO);

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", technician)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_TICKET))
                .andExpect(status().isForbidden());
    }

    // Entrada: el dispatch sugiere TEC-012; Ana crea el ticket y se recorre todo el ciclo.
    // Esperado: TEC-015 (no asignado) → 403; TEC-012 EN_PROGRESO → 200; RESUELTO sin observación → 400;
    // RESUELTO con observación → 200; Ana CERRADO → 200; el historial tiene 5 filas y la segunda es ASSIGNED.
    @Test
    @DisplayName("Técnico asignado: ASIGNADO → EN_PROGRESO → RESUELTO; otro técnico → 403; dueño cierra → 200")
    void assignedTechnicianWorksTheTicket() throws Exception {
        givenDispatchSuggests("TEC-012");
        String ana = TestTokens.forRole(mockMvc, Role.USUARIO);
        String assigned = TestTokens.forEmail(mockMvc, "tec012@helpflow.com");
        String other = TestTokens.forEmail(mockMvc, "tec015@helpflow.com");

        long ticketId = createTicket(ana);
        verify(dispatchPort).suggest(eq(ticketId), any(), any());
        String url = "/api/tickets/" + ticketId + "/status";

        mockMvc.perform(patch(url).header("Authorization", other)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("EN_PROGRESO", null)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch(url).header("Authorization", assigned)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("EN_PROGRESO", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.firstResponseAt").exists());

        // RESUELTO sin observación lo frena @ValidStatusChange
        mockMvc.perform(patch(url).header("Authorization", assigned)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("RESUELTO", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("observation"));

        mockMvc.perform(patch(url).header("Authorization", assigned)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("RESUELTO", "Se reinició el túnel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESUELTO"));

        mockMvc.perform(patch(url).header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("CERRADO", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CERRADO"));

        // CREATED, ASSIGNED, EN_PROGRESO, RESUELTO, CERRADO
        mockMvc.perform(get("/api/tickets/" + ticketId + "/history").header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[1].action").value("ASSIGNED"));
    }

    // Entrada: el ticket queda ASIGNADO a TEC-013 y Ana intenta cerrarlo de una vez.
    // Esperado: 409 "Invalid status transition" (solo se cierra un ticket RESUELTO).
    @Test
    @DisplayName("Usuario dueño intenta cerrar un ticket ASIGNADO → 409")
    void closingAssignedTicketReturns409() throws Exception {
        givenDispatchSuggests("TEC-013");
        String ana = TestTokens.forRole(mockMvc, Role.USUARIO);

        long ticketId = createTicket(ana);

        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status").header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody("CERRADO", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid status transition"));
    }

    // Entrada: el dispatch sugiere TEC-014, pero guardar el historial ASSIGNED falla a propósito.
    // Esperado: 500, y en la base no queda ni el ticket ni su historial CREATED: @Transactional deshizo todo.
    @Test
    @DisplayName("Rollback: si falla el historial de la asignación no queda el ticket a medias")
    void assignmentFailureRollsBackEverything() throws Exception {
        givenDispatchSuggests("TEC-014");
        // El repositorio es real, pero falla justo al guardar el historial ASSIGNED
        doThrow(new RuntimeException("simulated failure"))
                .when(ticketRepository)
                .saveHistory(argThat((TicketHistory history) ->
                        history != null && history.getAction() == HistoryAction.ASSIGNED));
        String ana = TestTokens.forRole(mockMvc, Role.USUARIO);
        Integer ticketsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM tickets", Integer.class);
        Integer historyBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM ticket_history", Integer.class);

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_TICKET))
                .andExpect(status().isInternalServerError());

        // Ni el ticket, ni el CREATED, ni la asignación quedaron guardados
        assertEquals(ticketsBefore, jdbcTemplate.queryForObject("SELECT count(*) FROM tickets", Integer.class));
        assertEquals(historyBefore, jdbcTemplate.queryForObject("SELECT count(*) FROM ticket_history", Integer.class));
    }
}
