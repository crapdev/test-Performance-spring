package com.riwi.supportticket.infrastructure.adapter.out.persistence.repository;

import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryJpaRepository extends JpaRepository<TicketHistoryEntity, Long> {

    // Por id y no por fecha: CREATED y ASSIGNED pueden tener la misma hora
    List<TicketHistoryEntity> findByTicketIdOrderByIdAsc(Long ticketId);
}
