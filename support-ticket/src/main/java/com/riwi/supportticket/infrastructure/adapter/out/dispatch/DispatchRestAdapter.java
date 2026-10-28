package com.riwi.supportticket.infrastructure.adapter.out.dispatch;

import com.riwi.supportticket.application.port.out.TechnicianDispatchPort;
import com.riwi.supportticket.domain.model.dispatch.DispatchSuggestion;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

// Cliente REST hacia technician-dispatch-service. Si no responde en 3 s lanza excepción
// y CreateTicketService deja el ticket ABIERTO.
@Component
public class DispatchRestAdapter implements TechnicianDispatchPort {

    private final RestClient restClient;

    // Se arma desde RestClient.Builder para que más adelante propague el traceId
    public DispatchRestAdapter(RestClient.Builder builder,
                               @Value("${dispatch.base-url}") String baseUrl,
                               @Value("${dispatch.timeout-seconds}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public DispatchSuggestion suggest(Long ticketId, Category category, Priority priority) {
        DispatchRequestDto request = new DispatchRequestDto(ticketId, category.name(), priority.name());
        DispatchResponseDto response = restClient.post()
                .uri("/technician-dispatch")
                .body(request)
                .retrieve()
                .body(DispatchResponseDto.class);
        if (response == null) {
            throw new IllegalStateException("Empty response from technician-dispatch-service");
        }
        return new DispatchSuggestion(response.ticketId(), response.tecnicoId(), response.disponibilidad(),
                response.tiempoEstimadoHoras());
    }
}
