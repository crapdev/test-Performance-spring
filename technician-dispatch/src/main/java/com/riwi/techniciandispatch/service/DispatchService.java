package com.riwi.techniciandispatch.service;

import com.riwi.techniciandispatch.dto.Categoria;
import com.riwi.techniciandispatch.dto.DispatchRequest;
import com.riwi.techniciandispatch.dto.DispatchResponse;
import com.riwi.techniciandispatch.dto.Prioridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Simula la central de despacho: mismos datos de entrada = misma respuesta
@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    public DispatchResponse dispatch(DispatchRequest request) {
        long seed = calculateSeed(request);
        String tecnicoId = chooseTechnician(request.categoria(), seed);
        int hours = estimateHours(request.prioridad(), seed);
        String disponibilidad = resolveAvailability(hours);
        log.info("Ticket {} ({}, {}) -> {} in {} h ({})", request.ticketId(), request.categoria(),
                request.prioridad(), tecnicoId, hours, disponibilidad);
        return new DispatchResponse(request.ticketId(), tecnicoId, disponibilidad, hours);
    }

    // Convertimos los tres datos en un número. Mismos datos = mismo número
    long calculateSeed(DispatchRequest request) {
        return request.ticketId() * 7
                + request.categoria().ordinal() * 3L
                + request.prioridad().ordinal();
    }

    // 5 técnicos por categoría (RED -> TEC-011 a TEC-015); OTRO puede ser cualquiera de los 20
    String chooseTechnician(Categoria categoria, long seed) {
        long technicianNumber;
        if (categoria == Categoria.OTRO) {
            technicianNumber = (seed % 20) + 1;
        } else {
            technicianNumber = categoria.ordinal() * 5L + (seed % 5) + 1;
        }
        return String.format("TEC-%03d", technicianNumber);
    }

    // Tiempo estimado según la prioridad: más urgente, menos horas
    int estimateHours(Prioridad prioridad, long seed) {
        if (prioridad == Prioridad.CRITICA) {
            return (int) (seed % 4);
        } else if (prioridad == Prioridad.ALTA) {
            return (int) (seed % 10);
        } else {
            return (int) (seed % 13);
        }
    }

    // 0-2 horas ALTA, 3-8 horas MEDIA, 9 o más BAJA
    String resolveAvailability(int hours) {
        if (hours <= 2) {
            return "ALTA";
        } else if (hours <= 8) {
            return "MEDIA";
        } else {
            return "BAJA";
        }
    }
}
