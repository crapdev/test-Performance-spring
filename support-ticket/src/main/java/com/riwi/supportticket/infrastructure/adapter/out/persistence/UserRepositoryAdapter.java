package com.riwi.supportticket.infrastructure.adapter.out.persistence;

import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.model.user.User;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository repository, UserPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        // Nuevo: entidad vacía. Existente: se carga para no perder datos que el dominio no trae
        UserEntity entity = new UserEntity();
        if (user.getId() != null) {
            entity = repository.findById(user.getId()).orElseThrow();
        }
        mapper.updateEntity(user, entity);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<UserEntity> found = repository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UserEntity> found = repository.findByEmail(email);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(found.get()));
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
