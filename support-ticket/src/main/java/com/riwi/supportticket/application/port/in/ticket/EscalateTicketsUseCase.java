package com.riwi.supportticket.application.port.in.ticket;

// Lo llama el SlaScheduler cada 5 minutos
public interface EscalateTicketsUseCase {

    // Devuelve cuántos tickets se escalaron en esta pasada
    int escalate();
}
