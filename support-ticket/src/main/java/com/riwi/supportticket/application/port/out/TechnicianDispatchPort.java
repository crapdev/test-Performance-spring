package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.dispatch.DispatchSuggestion;
import com.riwi.supportticket.domain.model.ticket.Category;
import com.riwi.supportticket.domain.model.ticket.Priority;

// Llamada a technician-dispatch-service. Si no responde, el adaptador lanza una RuntimeException.
public interface TechnicianDispatchPort {

    DispatchSuggestion suggest(Long ticketId, Category category, Priority priority);
}
