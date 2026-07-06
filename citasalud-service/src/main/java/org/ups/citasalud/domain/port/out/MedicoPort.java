package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Medico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicoPort {

    List<Medico> listarDisponiblesOnline(String especialidad);

    Optional<Medico> buscarPorId(UUID id);
}
