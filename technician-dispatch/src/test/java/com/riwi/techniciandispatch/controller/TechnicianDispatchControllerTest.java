package com.riwi.techniciandispatch.controller;

import com.riwi.techniciandispatch.service.DispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba del endpoint con MockMvc (@WebMvcTest): solo levanta la capa web y el DispatchService real.
// Revisa el contrato JSON del PDF y que los errores salgan como ProblemDetail.
@WebMvcTest(TechnicianDispatchController.class)
@Import(DispatchService.class)
class TechnicianDispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Entrada: POST /technician-dispatch con {ticketId: 125, categoria: RED, prioridad: ALTA}.
    // Esperado: 200 con {ticketId: 125, tecnicoId: TEC-014, disponibilidad: MEDIA, tiempoEstimadoHoras: 3}.
    @Test
    @DisplayName("Body válido → 200 con el técnico sugerido")
    void validRequestReturns200() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":125,\"categoria\":\"RED\",\"prioridad\":\"ALTA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(125))
                .andExpect(jsonPath("$.tecnicoId").value("TEC-014"))
                .andExpect(jsonPath("$.disponibilidad").value("MEDIA"))
                .andExpect(jsonPath("$.tiempoEstimadoHoras").value(3));
    }

    // Entrada: body sin "categoria".
    // Esperado: 400 en formato ProblemDetail, con traceId, timestamp y errors[0].field = "categoria".
    @Test
    @DisplayName("Falta un campo → 400 con ProblemDetail")
    void missingFieldReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":125,\"prioridad\":\"ALTA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors[0].field").value("categoria"));
    }

    // Entrada: "categoria": "IMPRESORA" (no existe en el enum).
    // Esperado: 400 (el JSON no se puede leer), nunca un 500.
    @Test
    @DisplayName("Categoría inexistente → 400")
    void unknownEnumValueReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":125,\"categoria\":\"IMPRESORA\",\"prioridad\":\"ALTA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
