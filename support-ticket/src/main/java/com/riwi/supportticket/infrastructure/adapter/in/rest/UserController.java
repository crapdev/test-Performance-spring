package com.riwi.supportticket.infrastructure.adapter.in.rest;

import com.riwi.supportticket.application.port.in.UpdateProfileUseCase;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.UpdateProfileRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.UserResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
public class UserController {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final UserWebMapper mapper;

    public UserController(UpdateProfileUseCase updateProfileUseCase, UserWebMapper mapper) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Ver mi perfil")
    public UserResponse getProfile(@AuthenticationPrincipal Actor actor) {
        return mapper.toResponse(updateProfileUseCase.getProfile(actor.getUserId()));
    }

    @PutMapping
    @Operation(summary = "Editar nombre, sede y área")
    public UserResponse updateProfile(@AuthenticationPrincipal Actor actor,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(updateProfileUseCase.updateProfile(actor.getUserId(), mapper.toCommand(request)));
    }
}
