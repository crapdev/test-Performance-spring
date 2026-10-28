package com.riwi.supportticket.infrastructure.adapter.in.rest.error;

import com.riwi.supportticket.application.port.out.TicketMetricsPort;
import com.riwi.supportticket.domain.exception.DomainException;
import com.riwi.supportticket.domain.exception.ObservationRequiredException;
import com.riwi.supportticket.domain.exception.ticket.InvalidStatusTransitionException;
import com.riwi.supportticket.domain.exception.ticket.TicketAccessDeniedException;
import com.riwi.supportticket.domain.exception.ticket.TicketNotFoundException;
import com.riwi.supportticket.domain.exception.user.DuplicateDocumentException;
import com.riwi.supportticket.domain.exception.user.DuplicateEmailException;
import com.riwi.supportticket.domain.exception.user.InactiveUserException;
import com.riwi.supportticket.domain.exception.user.InvalidCredentialsException;
import com.riwi.supportticket.domain.exception.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Todos los errores salen como ProblemDetail (RFC 7807) con timestamp y traceId.
// ResponseEntityExceptionHandler ya resuelve los errores propios de Spring MVC (JSON mal formado, 404 de ruta, 405...).
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://helpflow.com/problems/";

    private final TicketMetricsPort metrics;

    public GlobalExceptionHandler(TicketMetricsPort metrics) {
        this.metrics = metrics;
    }

    // ===== 400: Bean Validation =====
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = build(HttpStatus.BAD_REQUEST, "validation-error", "Validation failed",
                "The request has invalid fields");
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errors.add(error);
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    // ===== 401 =====
    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Invalid credentials", ex.getMessage());
    }

    // Llega desde ProblemDetailAuthenticationEntryPoint: sin token o token inválido
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized",
                "A valid Bearer token is required");
    }

    // ===== 403 =====
    @ExceptionHandler(TicketAccessDeniedException.class)
    public ProblemDetail handleTicketAccessDenied(TicketAccessDeniedException ex) {
        metrics.accessDenied();
        return build(HttpStatus.FORBIDDEN, "access-denied", "Access denied", ex.getMessage());
    }

    // @PreAuthorize o reglas de SecurityConfig: el rol no alcanza
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        metrics.accessDenied();
        return build(HttpStatus.FORBIDDEN, "access-denied", "Access denied",
                "Your role is not allowed to perform this action");
    }

    // ===== 404 =====
    @ExceptionHandler({UserNotFoundException.class, TicketNotFoundException.class})
    public ProblemDetail handleNotFound(DomainException ex) {
        return build(HttpStatus.NOT_FOUND, "not-found", "Resource not found", ex.getMessage());
    }

    // ===== 409 =====
    @ExceptionHandler({DuplicateDocumentException.class, DuplicateEmailException.class})
    public ProblemDetail handleDuplicate(DomainException ex) {
        return build(HttpStatus.CONFLICT, "duplicate-resource", "Duplicate resource", ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidStatusTransitionException ex) {
        return build(HttpStatus.CONFLICT, "invalid-status-transition", "Invalid status transition",
                ex.getMessage());
    }

    // Error de PostgreSQL (UNIQUE, FK...) traducido a un mensaje legible, sin mostrar el SQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage());
        String detail = "The data conflicts with an existing record";
        if (cause.contains("document")) {
            detail = "The document is already registered";
        } else if (cause.contains("email")) {
            detail = "The email is already registered";
        }
        return build(HttpStatus.CONFLICT, "data-conflict", "Data conflict", detail);
    }

    // ===== 422 =====
    @ExceptionHandler({InactiveUserException.class, ObservationRequiredException.class})
    public ProblemDetail handleBusinessRule(DomainException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, "business-rule", "Business rule violated", ex.getMessage());
    }

    // ===== 500: sin stacktrace en la respuesta =====
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal server error",
                "An unexpected error occurred");
    }

    // Los errores que resuelve ResponseEntityExceptionHandler también llevan timestamp y traceId
    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers,
                                                          HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            addCommonProperties(problem);
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ProblemDetail build(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setTitle(title);
        addCommonProperties(problem);
        return problem;
    }

    // "instance" lo completa Spring con la ruta de la petición
    private void addCommonProperties(ProblemDetail problem) {
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", currentTraceId());
    }

    // Con Micrometer Tracing el traceId viene en el MDC; si todavía no hay, se genera uno
    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return traceId;
    }
}
