-- Paciente adicional para test de concurrencia
INSERT INTO paciente (id, nombre, apellido, telefono_whatsapp) VALUES
('00000000-0000-0000-0000-000000000003', 'Pedro', 'Vargas Díaz', '+593987654323');

-- Franja exclusiva para test de concurrencia (ambas peticiones intentan reservar esta franja)
INSERT INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version) VALUES
('770e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001', '2026-09-01', '09:00', '09:30', 'DISPONIBLE', 0);

-- Franja ya OCUPADA para test directo 409 (Carlos Pérez, diferente a la de data.sql)
INSERT INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version) VALUES
('770e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440002', '2026-09-01', '14:00', '14:30', 'OCUPADA', 0);
