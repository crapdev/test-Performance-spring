package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.SlaPolicy;

import java.util.Optional;

public interface SlaPolicyRepositoryPort {

    Optional<SlaPolicy> findByPriority(Priority priority);
}
