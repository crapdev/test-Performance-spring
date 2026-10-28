package com.riwi.supportticket.infrastructure.adapter.out.persistence.repository;

import com.riwi.supportticket.domain.model.ticket.TicketStatus;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// @EntityGraph trae solicitante y técnico en la misma consulta (JOIN) y evita el N+1
public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"requester", "technician"})
    Optional<TicketEntity> findById(Long id);

    @EntityGraph(attributePaths = {"requester", "technician"})
    List<TicketEntity> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @EntityGraph(attributePaths = {"requester", "technician"})
    List<TicketEntity> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    @EntityGraph(attributePaths = {"requester", "technician"})
    List<TicketEntity> findAllByOrderByCreatedAtDesc();

    List<TicketEntity> findByStatusIn(Collection<TicketStatus> statuses);

    // Carga activa del técnico: ASIGNADO + EN_PROGRESO
    long countByTechnicianIdAndStatusIn(Long technicianId, Collection<TicketStatus> statuses);
}
