package com.riwi.supportticket.infrastructure.adapter.out.metrics;

import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

// Métricas de negocio. Se ven en /actuator/metrics/<nombre>
@Component
public class MicrometerTicketMetricsAdapter implements TicketMetricsPort {

    private final MeterRegistry registry;
    private final Timer firstResponseTimer;
    private final Timer resolutionTimer;
    private final Counter authFailures;
    private final Counter accessDenied;
    // El gauge lee este número cada vez que se consulta la métrica
    private final AtomicInteger overdueTickets = new AtomicInteger(0);

    public MicrometerTicketMetricsAdapter(MeterRegistry registry) {
        this.registry = registry;
        this.firstResponseTimer = Timer.builder("tickets.first_response.time")
                .description("Tiempo desde la creación hasta EN_PROGRESO")
                .register(registry);
        this.resolutionTimer = Timer.builder("tickets.resolution.time")
                .description("Tiempo desde la creación hasta RESUELTO")
                .register(registry);
        this.authFailures = Counter.builder("security.auth.failures")
                .description("Inicios de sesión fallidos")
                .register(registry);
        this.accessDenied = Counter.builder("security.access.denied")
                .description("Respuestas 403")
                .register(registry);
        registry.gauge("tickets.sla.overdue", overdueTickets);
    }

    @Override
    public void ticketCreated(Category category, Priority priority) {
        // Con tags se puede filtrar: /actuator/metrics/tickets.created?tag=category:RED
        Counter.builder("tickets.created")
                .tag("category", category.name())
                .tag("priority", priority.name())
                .register(registry)
                .increment();
    }

    @Override
    public void firstResponse(Duration time) {
        firstResponseTimer.record(time);
    }

    @Override
    public void resolution(Duration time) {
        resolutionTimer.record(time);
    }

    @Override
    public void overdueTickets(int count) {
        overdueTickets.set(count);
    }

    @Override
    public void authFailure() {
        authFailures.increment();
    }

    @Override
    public void accessDenied() {
        accessDenied.increment();
    }
}
