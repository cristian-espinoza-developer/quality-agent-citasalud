package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.domain.model.Cita;

import java.util.UUID;

import java.util.Optional;

public interface ReservarCitaUseCase {

    Cita reservar(UUID pacienteId, UUID franjaHorariaId);

    Optional<Cita> obtenerCita(UUID citaId);
}
