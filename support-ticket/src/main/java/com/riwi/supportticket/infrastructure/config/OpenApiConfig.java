package com.riwi.supportticket.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Habilita el botón "Authorize" de Swagger para pegar el JWT
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI helpFlowOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title("HelpFlow - support-ticket-service")
                        .description("Gestión de tickets de soporte TI con asignación automática y SLA")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes(SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }
}
