package com.riwi.supportticket.domain.model.user;

// Quién hace la petición. Se arma con los datos del JWT, sin ir a la base de datos.
public class Actor {

    private final Long userId; // Claim "sub" del JWT; se compara con el dueño o el técnico asignado
    private final Role role;   // Claim "role" del JWT; decide qué tickets ve y qué estados puede pedir

    public Actor(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }
}
