package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FranjaHorariaPort {

    Optional<FranjaHoraria> buscarConLockPesimista(UUID id);

    FranjaHoraria guardar(FranjaHoraria franjaHoraria);

    List<FranjaHoraria> buscarPorMedicoYFecha(UUID medicoId, LocalDate fecha);
}
