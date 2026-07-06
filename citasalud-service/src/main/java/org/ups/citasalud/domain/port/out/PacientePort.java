package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Paciente;

import java.util.Optional;
import java.util.UUID;

public interface PacientePort {

    Optional<Paciente> buscarPorId(UUID id);
}
