package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.RegisterUserCommand;
import com.riwi.supportticket.application.port.in.RegisterUserUseCase;
import com.riwi.supportticket.application.port.out.PasswordHasherPort;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.exception.user.DuplicateDocumentException;
import com.riwi.supportticket.domain.exception.user.DuplicateEmailException;
import com.riwi.supportticket.domain.model.user.Role;
import com.riwi.supportticket.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public RegisterUserService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByDocument(command.document())) {
            throw new DuplicateDocumentException(command.document());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        // Siempre activo y ROLE_USUARIO; técnicos y admin vienen del seed
        User user = new User(null, command.document(), command.fullName(), command.email(),
                command.sede(), command.area(), true, Role.USUARIO,
                passwordHasher.hash(command.password()));
        User saved = userRepository.save(user);
        log.info("User {} registered", saved.getId());
        return saved;
    }
}
