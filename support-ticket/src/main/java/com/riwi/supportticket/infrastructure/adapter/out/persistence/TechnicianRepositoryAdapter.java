package com.riwi.supportticket.infrastructure.adapter.out.persistence;

import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.TechnicianPersistenceMapper;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.TechnicianJpaRepository;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TechnicianRepositoryAdapter implements TechnicianRepositoryPort {

    // Estados que cuentan como carga activa del técnico
    private static final List<TicketStatus> ACTIVE_STATUSES = List.of(TicketStatus.ASIGNADO, TicketStatus.EN_PROGRESO);

    private final TechnicianJpaRepository repository;
    private final TicketJpaRepository ticketRepository;
    private final TechnicianPersistenceMapper mapper;

    public TechnicianRepositoryAdapter(TechnicianJpaRepository repository, TicketJpaRepository ticketRepository,
                                       TechnicianPersistenceMapper mapper) {
        this.repository = repository;
        this.ticketRepository = ticketRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Technician> findByCode(String code) {
        Optional<TechnicianEntity> found = repository.findByCode(code);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }

    @Override
    public Optional<Technician> findByUserId(Long userId) {
        Optional<TechnicianEntity> found = repository.findByUserId(userId);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }

    @Override
    public long countActiveTickets(Long technicianId) {
        return ticketRepository.countByTechnicianIdAndStatusIn(technicianId, ACTIVE_STATUSES);
    }
}
