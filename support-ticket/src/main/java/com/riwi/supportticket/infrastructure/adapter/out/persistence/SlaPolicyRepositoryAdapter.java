package com.riwi.supportticket.infrastructure.adapter.out.persistence;

import com.riwi.supportticket.application.port.out.SlaPolicyRepositoryPort;
import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.domain.model.ticket.SlaPolicy;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.TicketPersistenceMapper;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.SlaPolicyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SlaPolicyRepositoryAdapter implements SlaPolicyRepositoryPort {

    private final SlaPolicyJpaRepository repository;
    private final TicketPersistenceMapper mapper;

    public SlaPolicyRepositoryAdapter(SlaPolicyJpaRepository repository, TicketPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<SlaPolicy> findByPriority(Priority priority) {
        Optional<SlaPolicyEntity> found = repository.findById(priority);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }
}
