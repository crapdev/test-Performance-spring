package com.riwi.supportticket.infrastructure.adapter.out.persistence.repository;

import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByDocument(String document);

    boolean existsByEmail(String email);
}
