package com.riwi.supportticket.domain.model.ticket;

// El orden importa: de menor a mayor, para poder "subir un nivel"
public enum Priority {
    BAJA,
    MEDIA,
    ALTA,
    CRITICA;

    // Devuelve la prioridad siguiente. CRITICA ya es la más alta.
    public Priority next() {
        switch (this) {
            case BAJA:
                return MEDIA;
            case MEDIA:
                return ALTA;
            default:
                return CRITICA;
        }
    }
}
