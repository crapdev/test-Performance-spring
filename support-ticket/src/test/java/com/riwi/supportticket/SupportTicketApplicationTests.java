package com.riwi.supportticket;

import com.riwi.supportticket.application.port.in.ticket.CreateTicketUseCase;
import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Arranca la app completa contra PostgreSQL real (Testcontainers): Flyway V1–V3, ddl-auto=validate,
// BeanConfig con todos los casos de uso y todos los adaptadores conectados.
// jwt.secret fijo para no depender del .env de cada máquina.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "jwt.secret=test-secret-for-integration-tests-0123456789")
class SupportTicketApplicationTests {

    @Autowired
    private CreateTicketUseCase createTicketUseCase;

    @Autowired
    private TechnicianDispatchPort dispatchPort;

    // Entrada: arrancar la aplicación completa contra PostgreSQL real.
    // Esperado: el contexto levanta (Flyway aplica V1–V3, Hibernate valida las tablas) y los casos de uso
    // y adaptadores quedan conectados.
    @Test
    void contextLoads() {
        assertNotNull(createTicketUseCase);
        assertNotNull(dispatchPort);
    }

}
