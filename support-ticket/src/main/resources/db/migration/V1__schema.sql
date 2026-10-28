-- V1: usuarios, técnicos y tickets (sin asignación todavía)

-- Tipos ENUM de PostgreSQL: la base solo acepta estos valores (mismos nombres que los enums de Java)
CREATE TYPE user_role AS ENUM ('USUARIO', 'TECNICO', 'ADMIN');
CREATE TYPE technician_specialty AS ENUM ('HARDWARE', 'SOFTWARE', 'RED', 'ACCESOS');
CREATE TYPE ticket_category AS ENUM ('HARDWARE', 'SOFTWARE', 'RED', 'ACCESOS', 'OTRO');
CREATE TYPE ticket_priority AS ENUM ('BAJA', 'MEDIA', 'ALTA', 'CRITICA');
CREATE TYPE ticket_status AS ENUM ('ABIERTO', 'ASIGNADO', 'EN_PROGRESO', 'RESUELTO', 'CERRADO', 'CANCELADO');

CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    document  VARCHAR(20)  NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email     VARCHAR(120) NOT NULL UNIQUE,
    sede      VARCHAR(60)  NOT NULL,
    area      VARCHAR(60)  NOT NULL,
    active    BOOLEAN      NOT NULL DEFAULT TRUE, -- false = inactivo: no inicia sesión ni crea tickets
    role      user_role    NOT NULL,
    password  VARCHAR(100) NOT NULL -- hash BCrypt, nunca texto plano
);

-- El técnico también es un usuario (inicia sesión con ROLE_TECNICO)
CREATE TABLE technicians (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT               NOT NULL UNIQUE REFERENCES users (id),
    code               VARCHAR(10)          NOT NULL UNIQUE, -- TEC-001 ... TEC-020, lo que devuelve el dispatch
    specialty          technician_specialty NOT NULL,
    sede               VARCHAR(60)          NOT NULL,
    max_active_tickets INTEGER              NOT NULL CHECK (max_active_tickets > 0),
    salary             NUMERIC(12, 2)       NOT NULL,
    affiliation_date   DATE                 NOT NULL,
    active             BOOLEAN              NOT NULL DEFAULT TRUE -- false = no recibe asignaciones
);

CREATE TABLE tickets (
    id                BIGSERIAL PRIMARY KEY,
    requester_id      BIGINT          NOT NULL REFERENCES users (id),
    title             VARCHAR(120)    NOT NULL,
    description       TEXT            NOT NULL,
    category          ticket_category NOT NULL,
    priority          ticket_priority NOT NULL,
    sede              VARCHAR(60)     NOT NULL,
    affected_asset    VARCHAR(120)    NOT NULL,
    status            ticket_status   NOT NULL,
    created_at        TIMESTAMP       NOT NULL,
    sla_deadline      TIMESTAMP       NOT NULL,
    first_response_at TIMESTAMP,
    resolved_at       TIMESTAMP,
    escalated         BOOLEAN         NOT NULL DEFAULT FALSE
);

-- "Mis tickets" del usuario
CREATE INDEX idx_tickets_requester ON tickets (requester_id);
