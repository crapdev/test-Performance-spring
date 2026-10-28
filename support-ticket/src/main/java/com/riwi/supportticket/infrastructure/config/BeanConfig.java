package com.riwi.supportticket.infrastructure.config;

import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusUseCase;
import com.riwi.supportticket.application.port.in.ticket.CreateTicketUseCase;
import com.riwi.supportticket.application.port.in.ticket.EscalateTicketsUseCase;
import com.riwi.supportticket.application.port.in.LoginUseCase;
import com.riwi.supportticket.application.port.in.QueryTicketsUseCase;
import com.riwi.supportticket.application.port.in.RegisterUserUseCase;
import com.riwi.supportticket.application.port.in.UpdateProfileUseCase;
import com.riwi.supportticket.application.port.out.PasswordHasherPort;
import com.riwi.supportticket.application.port.out.SlaPolicyRepositoryPort;
import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import com.riwi.supportticket.application.port.out.TechnicianRepositoryPort;
import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.application.port.out.TicketRepositoryPort;
import com.riwi.supportticket.application.port.out.TokenPort;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.application.service.ChangeTicketStatusService;
import com.riwi.supportticket.application.service.CreateTicketService;
import com.riwi.supportticket.application.service.EscalateTicketsService;
import com.riwi.supportticket.application.service.LoginService;
import com.riwi.supportticket.application.service.QueryTicketsService;
import com.riwi.supportticket.application.service.RegisterUserService;
import com.riwi.supportticket.application.service.UpdateProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Los casos de uso no llevan @Service: se registran aquí pasándoles los puertos
@Configuration
public class BeanConfig {

    // Reloj del sistema; en las pruebas se usa uno fijo
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                                   PasswordHasherPort passwordHasher) {
        return new RegisterUserService(userRepository, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher,
                                     TokenPort tokenPort, TicketMetricsPort metrics) {
        return new LoginService(userRepository, passwordHasher, tokenPort, metrics);
    }

    @Bean
    public UpdateProfileUseCase updateProfileUseCase(UserRepositoryPort userRepository) {
        return new UpdateProfileService(userRepository);
    }

    @Bean
    public CreateTicketUseCase createTicketUseCase(UserRepositoryPort userRepository,
                                                   TicketRepositoryPort ticketRepository,
                                                   TechnicianRepositoryPort technicianRepository,
                                                   SlaPolicyRepositoryPort slaPolicyRepository,
                                                   TechnicianDispatchPort dispatchPort,
                                                   TicketMetricsPort metrics, Clock clock) {
        return new CreateTicketService(userRepository, ticketRepository, technicianRepository,
                slaPolicyRepository, dispatchPort, metrics, clock);
    }

    @Bean
    public ChangeTicketStatusUseCase changeTicketStatusUseCase(TicketRepositoryPort ticketRepository,
                                                               TechnicianRepositoryPort technicianRepository,
                                                               TicketMetricsPort metrics, Clock clock) {
        return new ChangeTicketStatusService(ticketRepository, technicianRepository, metrics, clock);
    }

    @Bean
    public QueryTicketsUseCase queryTicketsUseCase(TicketRepositoryPort ticketRepository,
                                                   TechnicianRepositoryPort technicianRepository) {
        return new QueryTicketsService(ticketRepository, technicianRepository);
    }

    @Bean
    public EscalateTicketsUseCase escalateTicketsUseCase(TicketRepositoryPort ticketRepository,
                                                         TicketMetricsPort metrics, Clock clock,
                                                         @Value("${sla.escalation-percentage}") int percentage) {
        return new EscalateTicketsService(ticketRepository, metrics, clock, percentage);
    }
}
