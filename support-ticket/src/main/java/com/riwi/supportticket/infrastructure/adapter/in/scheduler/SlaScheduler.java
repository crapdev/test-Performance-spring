package com.riwi.supportticket.infrastructure.adapter.in.scheduler;

import com.riwi.supportticket.application.port.in.ticket.EscalateTicketsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Adaptador de entrada: en vez de un controlador REST, lo dispara el reloj
@Component
public class SlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaScheduler.class);

    private final EscalateTicketsUseCase escalateTicketsUseCase;

    public SlaScheduler(EscalateTicketsUseCase escalateTicketsUseCase) {
        this.escalateTicketsUseCase = escalateTicketsUseCase;
    }

    // Cada 5 minutos (sla.check-interval-ms)
    @Scheduled(fixedRateString = "${sla.check-interval-ms}")
    public void checkSla() {
        int escalated = escalateTicketsUseCase.escalate();
        log.info("SLA check finished: {} ticket(s) escalated", escalated);
    }
}
