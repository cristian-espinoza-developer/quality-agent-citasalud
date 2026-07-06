package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConsultarDisponibilidadUseCase {

    List<Medico> listarMedicosDisponibles(String especialidad);

    List<FranjaHoraria> consultarDisponibilidad(UUID medicoId, LocalDate fecha);
}
