package com.riwi.supportticket.infrastructure.adapter.out.persistence.repository;

import com.riwi.supportticket.domain.model.ticket.Priority;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyJpaRepository extends JpaRepository<SlaPolicyEntity, Priority> {
}
