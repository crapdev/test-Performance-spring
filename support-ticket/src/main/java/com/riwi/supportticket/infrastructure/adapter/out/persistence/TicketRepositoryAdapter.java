package com.riwi.supportticket.infrastructure.adapter.out.persistence;

import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.TicketPersistenceMapper;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.TechnicianJpaRepository;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.TicketHistoryJpaRepository;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TicketRepositoryAdapter implements TicketRepositoryPort {

    // Tickets vivos: todavía cuentan para el SLA
    private static final List<TicketStatus> ALIVE_STATUSES =
            List.of(TicketStatus.ABIERTO, TicketStatus.ASIGNADO, TicketStatus.EN_PROGRESO);

    private final TicketJpaRepository ticketRepository;
    private final TicketHistoryJpaRepository historyRepository;
    private final UserJpaRepository userRepository;
    private final TechnicianJpaRepository technicianRepository;
    private final TicketPersistenceMapper mapper;

    public TicketRepositoryAdapter(TicketJpaRepository ticketRepository, TicketHistoryJpaRepository historyRepository,
                                   UserJpaRepository userRepository, TechnicianJpaRepository technicianRepository,
                                   TicketPersistenceMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
        this.mapper = mapper;
    }

    @Override
    public Ticket save(Ticket ticket) {
        // Nuevo: entidad vacía. Existente: se carga para no perder el historial ni las relaciones
        TicketEntity entity = new TicketEntity();
        if (ticket.getId() != null) {
            entity = ticketRepository.findById(ticket.getId()).orElseThrow();
        }
        mapper.updateEntity(ticket, entity);

        // getReferenceById no consulta la base de datos: solo arma la relación con el id
        entity.setRequester(userRepository.getReferenceById(ticket.getRequesterId()));
        if (ticket.getTechnicianId() != null) {
            entity.setTechnician(technicianRepository.getReferenceById(ticket.getTechnicianId()));
        } else {
            entity.setTechnician(null);
        }
        return mapper.toDomain(ticketRepository.save(entity));
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        Optional<TicketEntity> found = ticketRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }

    @Override
    public List<Ticket> findByRequesterId(Long requesterId) {
        return toDomainList(ticketRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId));
    }

    @Override
    public List<Ticket> findByTechnicianId(Long technicianId) {
        return toDomainList(ticketRepository.findByTechnicianIdOrderByCreatedAtDesc(technicianId));
    }

    @Override
    public List<Ticket> findAll() {
        return toDomainList(ticketRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<Ticket> findAlive() {
        return toDomainList(ticketRepository.findByStatusIn(ALIVE_STATUSES));
    }

    @Override
    public TicketHistory saveHistory(TicketHistory history) {
        TicketHistoryEntity entity = mapper.toEntity(history);
        entity.setTicket(ticketRepository.getReferenceById(history.getTicketId()));
        return mapper.toDomain(historyRepository.save(entity));
    }

    @Override
    public List<TicketHistory> findHistoryByTicketId(Long ticketId) {
        List<TicketHistory> result = new ArrayList<>();
        for (TicketHistoryEntity entity : historyRepository.findByTicketIdOrderByIdAsc(ticketId)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }

    private List<Ticket> toDomainList(List<TicketEntity> entities) {
        List<Ticket> result = new ArrayList<>();
        for (TicketEntity entity : entities) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }
}
