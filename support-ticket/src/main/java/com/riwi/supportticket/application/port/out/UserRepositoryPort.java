package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByDocument(String document);

    boolean existsByEmail(String email);
}
