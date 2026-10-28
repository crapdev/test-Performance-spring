package com.riwi.supportticket.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba de integración: app completa + PostgreSQL real (Testcontainers) + llamadas HTTP con MockMvc.
// Registro, login y protección de rutas con JWT.
class AuthIntegrationTest extends IntegrationTestBase {

    private String registerBody(String document, String email) {
        return "{\"document\":\"" + document + "\",\"fullName\":\"Laura Test\",\"email\":\"" + email + "\","
                + "\"sede\":\"Medellín\",\"area\":\"Compras\",\"password\":\"secreto1\"}";
    }

    // Entrada: POST /auth/register, luego POST /auth/login con esa clave, luego GET /api/users/me con el token.
    // Esperado: 201 (rol USUARIO, active = true, sin password en la respuesta) → 200 con token Bearer → 200 con su correo.
    @Test
    @DisplayName("Registro → login → el token sirve para ver el perfil")
    void registerLoginAndUseToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("70000001", "laura@helpflow.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USUARIO"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"laura@helpflow.com\",\"password\":\"secreto1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.accessToken");

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("laura@helpflow.com"));
    }

    // Entrada: login de Ana con una clave incorrecta.
    // Esperado: 401 en formato ProblemDetail, con traceId y timestamp.
    @Test
    @DisplayName("Login con clave mala → 401 con ProblemDetail")
    void wrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@helpflow.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // Entrada: GET /api/tickets sin header Authorization.
    // Esperado: 401 con instance = "/api/tickets".
    @Test
    @DisplayName("Sin token → 401")
    void missingTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("/api/tickets"));
    }

    // Entrada: GET /api/tickets con un token inventado ("abc.def.ghi").
    // Esperado: 401 (la firma no es válida).
    @Test
    @DisplayName("Token inválido → 401")
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/tickets").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    // Entrada: registro con el documento 1000000001 (ya es de Ana en el seed).
    // Esperado: 409.
    @Test
    @DisplayName("Documento duplicado → 409")
    void duplicateDocumentReturns409() throws Exception {
        // 1000000001 es Ana en el seed
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("1000000001", "otra.ana@helpflow.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // Entrada: registro con laura@gmail.com.
    // Esperado: 400 con errors[0].field = "email" (solo se aceptan correos @helpflow.com).
    @Test
    @DisplayName("Correo que no es @helpflow.com → 400 con el campo en errors")
    void nonCorporateEmailReturns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("70000002", "laura@gmail.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }
}
