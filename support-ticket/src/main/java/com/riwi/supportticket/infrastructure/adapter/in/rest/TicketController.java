package com.riwi.supportticket.infrastructure.adapter.in.rest;

import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusUseCase;
import com.riwi.supportticket.application.port.in.ticket.CreateTicketUseCase;
import com.riwi.supportticket.application.port.in.QueryTicketsUseCase;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.ChangeStatusRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.CreateTicketRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.TicketHistoryResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.TicketResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.mapper.TicketWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

// Spring Security valida el rol; que el ticket sea suyo o esté asignado a él lo valida el caso de uso
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Crear, consultar y cambiar el estado de los tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final QueryTicketsUseCase queryTicketsUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final TicketWebMapper mapper;

    public TicketController(CreateTicketUseCase createTicketUseCase, QueryTicketsUseCase queryTicketsUseCase,
                            ChangeTicketStatusUseCase changeTicketStatusUseCase, TicketWebMapper mapper) {
        this.createTicketUseCase = createTicketUseCase;
        this.queryTicketsUseCase = queryTicketsUseCase;
        this.changeTicketStatusUseCase = changeTicketStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Crear ticket (solo USUARIO); se asigna con technician-dispatch-service")
    public ResponseEntity<TicketResponse> create(@AuthenticationPrincipal Actor actor,
                                                 @Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = createTicketUseCase.create(actor.getUserId(), mapper.toCommand(request));
        return ResponseEntity.created(URI.create("/api/tickets/" + ticket.getId())).body(mapper.toResponse(ticket));
    }

    @GetMapping
    @Operation(summary = "Listar: USUARIO los suyos, TECNICO los asignados, ADMIN todos")
    public List<TicketResponse> findAll(@AuthenticationPrincipal Actor actor) {
        return mapper.toResponseList(queryTicketsUseCase.findAll(actor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle (dueño, técnico asignado o admin)")
    public TicketResponse findById(@AuthenticationPrincipal Actor actor, @PathVariable Long id) {
        return mapper.toResponse(queryTicketsUseCase.findById(actor, id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historial (dueño, técnico asignado o admin)")
    public List<TicketHistoryResponse> findHistory(@AuthenticationPrincipal Actor actor, @PathVariable Long id) {
        return mapper.toHistoryResponseList(queryTicketsUseCase.findHistory(actor, id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado (técnico asignado o usuario dueño)")
    public TicketResponse changeStatus(@AuthenticationPrincipal Actor actor, @PathVariable Long id,
                                       @Valid @RequestBody ChangeStatusRequest request) {
        return mapper.toResponse(changeTicketStatusUseCase.changeStatus(actor, id, mapper.toCommand(request)));
    }
}
