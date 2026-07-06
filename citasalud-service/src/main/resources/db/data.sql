-- Médicos habilitados para reserva online
INSERT INTO medico (id, nombre, apellido, especialidad, disponible_online) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Ana', 'García López', 'Medicina General', true),
('550e8400-e29b-41d4-a716-446655440002', 'Carlos', 'Pérez Medina', 'Cardiología', true);

-- Pacientes registrados con WhatsApp
INSERT INTO paciente (id, nombre, apellido, telefono_whatsapp) VALUES
('00000000-0000-0000-0000-000000000001', 'Juan', 'Rodríguez Torres', '+593987654321'),
('00000000-0000-0000-0000-000000000002', 'María', 'Santos Villalba', '+593987654322');

-- Franjas horarias 2026-07-15 — Dr. Ana García (mezcla DISPONIBLE/OCUPADA)
INSERT INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version) VALUES
('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '2026-07-15', '09:00', '09:30', 'DISPONIBLE', 0),
('770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', '2026-07-15', '09:30', '10:00', 'DISPONIBLE', 0),
('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', '2026-07-15', '10:00', '10:30', 'OCUPADA', 0);

-- Franjas horarias 2026-08-01 — Dr. Ana García
INSERT INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version) VALUES
('770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', '2026-08-01', '09:00', '09:30', 'DISPONIBLE', 0),
('770e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440001', '2026-08-01', '09:30', '10:00', 'DISPONIBLE', 0);

-- Franja horaria 2026-07-15 — Dr. Carlos Pérez
INSERT INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version) VALUES
('770e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440002', '2026-07-15', '14:00', '14:30', 'DISPONIBLE', 0),
('770e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', '2026-07-15', '14:30', '15:00', 'DISPONIBLE', 0);
