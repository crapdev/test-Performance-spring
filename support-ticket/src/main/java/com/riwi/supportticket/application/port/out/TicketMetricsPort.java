package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;

import java.time.Duration;

// Métricas de negocio sin que la aplicación conozca Micrometer
public interface TicketMetricsPort {

    void ticketCreated(Category category, Priority priority);

    void firstResponse(Duration time);

    void resolution(Duration time);

    void overdueTickets(int count);

    void authFailure();

    void accessDenied();
}
