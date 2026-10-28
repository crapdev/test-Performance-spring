package com.riwi.supportticket.infrastructure.adapter.in.rest;

import com.riwi.supportticket.application.port.in.LoginUseCase;
import com.riwi.supportticket.application.port.in.RegisterUserUseCase;
import com.riwi.supportticket.domain.model.user.User;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.RegisterRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.TokenResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.UserResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Registro e inicio de sesión (públicos)")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final UserWebMapper mapper;
    private final long expirationMinutes;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase, UserWebMapper mapper,
                          @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.mapper = mapper;
        this.expirationMinutes = expirationMinutes;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un usuario (siempre ROLE_USUARIO)")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.register(mapper.toCommand(request));
        return mapper.toResponse(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Devuelve el JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.login(request.email(), request.password());
        return new TokenResponse(token, "Bearer", expirationMinutes * 60);
    }
}
