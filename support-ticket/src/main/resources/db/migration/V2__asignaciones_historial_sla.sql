-- V2: asignación de técnico, historial y políticas de SLA

CREATE TYPE history_action AS ENUM ('CREATED', 'ASSIGNED', 'NOT_ASSIGNED', 'STATUS_CHANGED', 'ESCALATED');

-- null mientras el ticket esté ABIERTO
ALTER TABLE tickets ADD COLUMN technician_id BIGINT REFERENCES technicians (id);

-- "Tickets asignados" y conteo de carga activa (ASIGNADO + EN_PROGRESO)
CREATE INDEX idx_tickets_technician_status ON tickets (technician_id, status);
-- El escalamiento recorre los tickets vivos
CREATE INDEX idx_tickets_status ON tickets (status);

CREATE TABLE ticket_history (
    id           BIGSERIAL PRIMARY KEY,
    ticket_id    BIGINT         NOT NULL REFERENCES tickets (id),
    action       history_action NOT NULL,
    from_status  ticket_status, -- null en CREATED
    to_status    ticket_status  NOT NULL,
    observation  TEXT,
    performed_by BIGINT REFERENCES users (id), -- null cuando lo hace el sistema
    performed_at TIMESTAMP      NOT NULL
);

CREATE INDEX idx_ticket_history_ticket ON ticket_history (ticket_id);

-- Una fila por prioridad
CREATE TABLE sla_policies (
    priority         ticket_priority PRIMARY KEY,
    resolution_hours INTEGER         NOT NULL CHECK (resolution_hours > 0)
);
