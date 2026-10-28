package com.riwi.supportticket.integration;

import com.jayway.jsonpath.JsonPath;
import com.riwi.supportticket.domain.model.user.Role;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Hace login con los usuarios del seed (V3) y devuelve el header "Bearer <token>"
public final class TestTokens {

    // Contraseña de todos los usuarios sembrados
    public static final String SEED_PASSWORD = "123456";

    private TestTokens() {
    }

    // Un usuario representativo por rol
    public static String forRole(MockMvc mockMvc, Role role) throws Exception {
        if (role == Role.ADMIN) {
            return forEmail(mockMvc, "admin@helpflow.com");
        }
        if (role == Role.TECNICO) {
            return forEmail(mockMvc, "tec011@helpflow.com");
        }
        return forEmail(mockMvc, "ana@helpflow.com");
    }

    public static String forEmail(MockMvc mockMvc, String email) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + SEED_PASSWORD + "\"}";
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.accessToken");
        return "Bearer " + token;
    }
}
