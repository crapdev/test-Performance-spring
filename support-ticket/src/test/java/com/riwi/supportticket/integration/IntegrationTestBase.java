package com.riwi.supportticket.integration;

import com.riwi.supportticket.TestcontainersConfiguration;
import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

// Base común: app completa + PostgreSQL real + MockMvc.
// Todas las clases hijas comparten el mismo contexto (y el mismo contenedor) porque usan los mismos mocks.
//  - El dispatch es un mock: las pruebas no dependen de que el otro servicio esté arriba.
//  - El repositorio de tickets es un spy: funciona de verdad, pero se puede forzar un error (prueba de rollback).
// Los mocks se resetean solos después de cada prueba.
@SpringBootTest(properties = "jwt.secret=test-secret-for-integration-tests-0123456789")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockitoBean
    protected TechnicianDispatchPort dispatchPort;

    @MockitoSpyBean
    protected TicketRepositoryPort ticketRepository;
}
