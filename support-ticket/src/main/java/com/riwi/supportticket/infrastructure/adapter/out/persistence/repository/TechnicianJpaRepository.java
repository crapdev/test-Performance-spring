package com.riwi.supportticket.infrastructure.adapter.out.persistence.repository;

import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicianJpaRepository extends JpaRepository<TechnicianEntity, Long> {

    Optional<TechnicianEntity> findByCode(String code);

    // Navega technician.user.id
    Optional<TechnicianEntity> findByUserId(Long userId);
}
