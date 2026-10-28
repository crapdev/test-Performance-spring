package com.riwi.supportticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling activa el SlaScheduler (escalamiento cada 5 minutos).
// Se excluye UserDetailsServiceAutoConfiguration: la autenticación es con JWT y los usuarios están en la base,
// así que el usuario "user" en memoria que crea Spring Boot sobra (y escribiría su contraseña en los logs).
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class SupportTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportTicketApplication.class, args);
    }

}
