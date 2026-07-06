package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Cita;

import java.util.Optional;
import java.util.UUID;

public interface CitaPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);
}
