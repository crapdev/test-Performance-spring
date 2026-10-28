package com.riwi.techniciandispatch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Todos los errores salen en formato ProblemDetail (RFC 7807) con timestamp y traceId
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Falta un campo obligatorio (@NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "validation-error",
                "Validation failed", "The request body has invalid fields");

        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errors.add(error);
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    // JSON mal formado o valor que no existe en el enum (ej.: "categoria": "IMPRESORA")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "malformed-request",
                "Malformed request", "The request body could not be read");
    }

    // Cualquier otro error: 500 sin stacktrace
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error in dispatch", ex);
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "Internal server error", "An unexpected error occurred");
    }

    private ProblemDetail buildProblem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://helpflow.com/problems/" + type));
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", currentTraceId());
        return problem;
    }

    // Micrometer Tracing deja el traceId en el MDC (el mismo que mandó support-ticket-service)
    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return traceId;
    }
}
