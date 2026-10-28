-- V3: datos iniciales
-- Contraseña de todos los usuarios sembrados: 123456 (hash BCrypt)

-- Admin y usuarios demo
INSERT INTO users (document, full_name, email, sede, area, active, role, password) VALUES
    ('1000000000', 'Administrador HelpFlow', 'admin@helpflow.com',   'Barranquilla', 'Tecnología',   TRUE, 'ADMIN',   '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('1000000001', 'Ana Pérez',              'ana@helpflow.com',     'Barranquilla', 'Contabilidad', TRUE, 'USUARIO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('1000000002', 'Carlos Gómez',           'carlos@helpflow.com',  'Bogotá',       'Ventas',       TRUE, 'USUARIO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS');

-- Usuarios de los técnicos (inician sesión con ROLE_TECNICO). TEC-020 está inactivo (active = FALSE).
INSERT INTO users (document, full_name, email, sede, area, active, role, password) VALUES
    ('2000000001', 'Luis Herrera', 'tec001@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000002', 'María Torres', 'tec002@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000003', 'Jorge Díaz', 'tec003@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000004', 'Paula Rojas', 'tec004@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000005', 'Andrés Ruiz', 'tec005@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000006', 'Laura Castro', 'tec006@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000007', 'Diego Mora', 'tec007@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000008', 'Sofía Vargas', 'tec008@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000009', 'Felipe Ríos', 'tec009@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000010', 'Valentina Gil', 'tec010@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000011', 'Camilo Ortiz', 'tec011@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000012', 'Daniela Páez', 'tec012@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000013', 'Sergio León', 'tec013@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000014', 'Natalia Cruz', 'tec014@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000015', 'Julián Mejía', 'tec015@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000016', 'Carolina Silva', 'tec016@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000017', 'Mateo Suárez', 'tec017@helpflow.com', 'Bogotá', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000018', 'Isabela Rey', 'tec018@helpflow.com', 'Medellín', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000019', 'Santiago Lara', 'tec019@helpflow.com', 'Barranquilla', 'Soporte TI', TRUE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS'),
    ('2000000020', 'Gabriela Niño', 'tec020@helpflow.com', 'Bogotá', 'Soporte TI', FALSE, 'TECNICO', '$2a$10$bt4keBo30pti58zNiJT7AejTuVHIwwxJTNPz7F7TkAUreglks5kbS');

-- Técnicos: 5 por especialidad, sede rotando, carga máxima 3
-- Se enlazan con su usuario por el correo
-- Los valores de VALUES llegan como texto: specialty se convierte al tipo ENUM con ::technician_specialty
INSERT INTO technicians (user_id, code, specialty, sede, max_active_tickets, salary, affiliation_date, active)
SELECT u.id, t.code, t.specialty::technician_specialty, t.sede, 3, t.salary, t.affiliation_date, t.active
FROM (VALUES
    ('TEC-001', 'HARDWARE', 'Barranquilla', 3250000.00, DATE '2024-01-15', TRUE, 'tec001@helpflow.com'),
    ('TEC-002', 'HARDWARE', 'Bogotá', 3500000.00, DATE '2024-02-15', TRUE, 'tec002@helpflow.com'),
    ('TEC-003', 'HARDWARE', 'Medellín', 3750000.00, DATE '2024-03-15', TRUE, 'tec003@helpflow.com'),
    ('TEC-004', 'HARDWARE', 'Barranquilla', 4000000.00, DATE '2024-04-15', TRUE, 'tec004@helpflow.com'),
    ('TEC-005', 'HARDWARE', 'Bogotá', 3000000.00, DATE '2024-05-15', TRUE, 'tec005@helpflow.com'),
    ('TEC-006', 'SOFTWARE', 'Medellín', 3250000.00, DATE '2024-06-15', TRUE, 'tec006@helpflow.com'),
    ('TEC-007', 'SOFTWARE', 'Barranquilla', 3500000.00, DATE '2024-07-15', TRUE, 'tec007@helpflow.com'),
    ('TEC-008', 'SOFTWARE', 'Bogotá', 3750000.00, DATE '2024-08-15', TRUE, 'tec008@helpflow.com'),
    ('TEC-009', 'SOFTWARE', 'Medellín', 4000000.00, DATE '2024-09-15', TRUE, 'tec009@helpflow.com'),
    ('TEC-010', 'SOFTWARE', 'Barranquilla', 3000000.00, DATE '2024-10-15', TRUE, 'tec010@helpflow.com'),
    ('TEC-011', 'RED', 'Bogotá', 3250000.00, DATE '2024-11-15', TRUE, 'tec011@helpflow.com'),
    ('TEC-012', 'RED', 'Medellín', 3500000.00, DATE '2024-12-15', TRUE, 'tec012@helpflow.com'),
    ('TEC-013', 'RED', 'Barranquilla', 3750000.00, DATE '2024-01-15', TRUE, 'tec013@helpflow.com'),
    ('TEC-014', 'RED', 'Bogotá', 4000000.00, DATE '2024-02-15', TRUE, 'tec014@helpflow.com'),
    ('TEC-015', 'RED', 'Medellín', 3000000.00, DATE '2024-03-15', TRUE, 'tec015@helpflow.com'),
    ('TEC-016', 'ACCESOS', 'Barranquilla', 3250000.00, DATE '2024-04-15', TRUE, 'tec016@helpflow.com'),
    ('TEC-017', 'ACCESOS', 'Bogotá', 3500000.00, DATE '2024-05-15', TRUE, 'tec017@helpflow.com'),
    ('TEC-018', 'ACCESOS', 'Medellín', 3750000.00, DATE '2024-06-15', TRUE, 'tec018@helpflow.com'),
    ('TEC-019', 'ACCESOS', 'Barranquilla', 4000000.00, DATE '2024-07-15', TRUE, 'tec019@helpflow.com'),
    ('TEC-020', 'ACCESOS', 'Bogotá', 3000000.00, DATE '2024-08-15', FALSE, 'tec020@helpflow.com')
) AS t (code, specialty, sede, salary, affiliation_date, active, email)
JOIN users u ON u.email = t.email;

-- Horas máximas de resolución por prioridad
INSERT INTO sla_policies (priority, resolution_hours) VALUES
    ('CRITICA', 2),
    ('ALTA', 8),
    ('MEDIA', 24),
    ('BAJA', 48);
