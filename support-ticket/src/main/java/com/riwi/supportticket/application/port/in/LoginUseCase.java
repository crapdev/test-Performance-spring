package com.riwi.supportticket.application.port.in;

public interface LoginUseCase {

    // Devuelve el JWT si el correo y la clave son correctos y el usuario está activo
    String login(String email, String password);
}
