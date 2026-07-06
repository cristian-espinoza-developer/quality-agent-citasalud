package org.ups.citasalud.application.usecase;

import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;
import org.ups.citasalud.domain.port.out.MedicoPort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private final MedicoPort medicoPort;
    private final FranjaHorariaPort franjaHorariaPort;

    public ConsultarDisponibilidadService(MedicoPort medicoPort, FranjaHorariaPort franjaHorariaPort) {
        this.medicoPort = medicoPort;
        this.franjaHorariaPort = franjaHorariaPort;
    }

    @Override
    public List<Medico> listarMedicosDisponibles(String especialidad) {
        return medicoPort.listarDisponiblesOnline(especialidad);
    }

    @Override
    public List<FranjaHoraria> consultarDisponibilidad(UUID medicoId, LocalDate fecha) {
        medicoPort.buscarPorId(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Médico no encontrado o no disponible para reserva online: " + medicoId));
        return franjaHorariaPort.buscarPorMedicoYFecha(medicoId, fecha);
    }
}
