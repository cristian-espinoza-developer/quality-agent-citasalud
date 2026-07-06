package org.ups.citasalud.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Cita {

    private final UUID id;
    private final UUID pacienteId;
    private final FranjaHoraria franjaHoraria;
    private EstadoCita estado;
    private final LocalDateTime fechaCreacion;

    private Cita(UUID id, UUID pacienteId, FranjaHoraria franjaHoraria,
                 EstadoCita estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.franjaHoraria = franjaHoraria;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public static Cita confirmar(UUID pacienteId, FranjaHoraria franjaHoraria) {
        return new Cita(
                UUID.randomUUID(),
                pacienteId,
                franjaHoraria,
                EstadoCita.CONFIRMADA,
                LocalDateTime.now()
        );
    }

    public static Cita reconstruir(UUID id, UUID pacienteId, FranjaHoraria franjaHoraria,
                                    EstadoCita estado, LocalDateTime fechaCreacion) {
        return new Cita(id, pacienteId, franjaHoraria, estado, fechaCreacion);
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public FranjaHoraria getFranjaHoraria() { return franjaHoraria; }
    public EstadoCita getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
