CREATE TABLE IF NOT EXISTS medico (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    disponible_online BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS paciente (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono_whatsapp VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS franja_horaria (
    id UUID PRIMARY KEY,
    medico_id UUID NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_franja_medico FOREIGN KEY (medico_id) REFERENCES medico(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_franja_medico_fecha_hora
    ON franja_horaria (medico_id, fecha, hora_inicio);

CREATE TABLE IF NOT EXISTS cita (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    franja_horaria_id UUID NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
    fecha_creacion TIMESTAMP NOT NULL,
    CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id),
    CONSTRAINT fk_cita_franja FOREIGN KEY (franja_horaria_id) REFERENCES franja_horaria(id)
);
