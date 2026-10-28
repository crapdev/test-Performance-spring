package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.LoginUseCase;
import com.riwi.supportticket.application.port.out.PasswordHasherPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TokenPort;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.exception.user.InactiveUserException;
import com.riwi.supportticket.domain.exception.user.InvalidCredentialsException;
import com.riwi.supportticket.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LoginService implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenPort tokenPort;
    private final TicketMetricsPort metrics;

    public LoginService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher,
                        TokenPort tokenPort, TicketMetricsPort metrics) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenPort = tokenPort;
        this.metrics = metrics;
    }

    @Override
    public String login(String email, String password) {
        Optional<User> found = userRepository.findByEmail(email);
        // Mismo error si el correo no existe o la clave está mal
        if (found.isEmpty()) {
            log.warn("Failed login: unknown email");
            metrics.authFailure();
            throw new InvalidCredentialsException();
        }
        User user = found.get();
        if (!passwordHasher.matches(password, user.getPassword())) {
            log.warn("Failed login: wrong password for user {}", user.getId());
            metrics.authFailure();
            throw new InvalidCredentialsException();
        }
        // Se revisa después de la clave para no revelar el estado de cuentas ajenas
        if (!user.isActive()) {
            log.warn("Failed login: user {} is inactive", user.getId());
            metrics.authFailure();
            throw new InactiveUserException(user.getId());
        }
        log.info("User {} logged in ({})", user.getId(), user.getRole());
        return tokenPort.generate(user);
    }
}
