package org.ups.citasalud.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.ups.citasalud.application.event.CitaConfirmadaEvent;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.model.NotificacionConfirmacion;
import org.ups.citasalud.domain.model.Paciente;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.out.CitaPort;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;
import org.ups.citasalud.domain.port.out.MedicoPort;
import org.ups.citasalud.domain.port.out.PacientePort;

import java.util.Optional;
import java.util.UUID;

public class ReservarCitaService implements ReservarCitaUseCase {

    private final CitaPort citaPort;
    private final FranjaHorariaPort franjaHorariaPort;
    private final PacientePort pacientePort;
    private final MedicoPort medicoPort;
    private final ApplicationEventPublisher eventPublisher;

    public ReservarCitaService(CitaPort citaPort, FranjaHorariaPort franjaHorariaPort,
                                PacientePort pacientePort, MedicoPort medicoPort,
                                ApplicationEventPublisher eventPublisher) {
        this.citaPort = citaPort;
        this.franjaHorariaPort = franjaHorariaPort;
        this.pacientePort = pacientePort;
        this.medicoPort = medicoPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Cita reservar(UUID pacienteId, UUID franjaHorariaId) {
        Paciente paciente = pacientePort.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Paciente no encontrado: " + pacienteId));

        FranjaHoraria franja = franjaHorariaPort.buscarConLockPesimista(franjaHorariaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Franja horaria no encontrada: " + franjaHorariaId));

        franja.reservar();
        franjaHorariaPort.guardar(franja);

        Cita cita = Cita.confirmar(pacienteId, franja);
        Cita citaGuardada = citaPort.guardar(cita);

        Medico medico = medicoPort.buscarPorId(franja.getMedicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Médico no encontrado: " + franja.getMedicoId()));

        NotificacionConfirmacion notificacion = new NotificacionConfirmacion(
                citaGuardada.getId(),
                paciente.getNombre() + " " + paciente.getApellido(),
                paciente.getTelefonoWhatsApp(),
                medico.getNombre() + " " + medico.getApellido(),
                medico.getEspecialidad(),
                franja.getFecha(),
                franja.getHoraInicio(),
                franja.getHoraFin()
        );

        eventPublisher.publishEvent(new CitaConfirmadaEvent(this, notificacion));
        return citaGuardada;
    }

    @Override
    public Optional<Cita> obtenerCita(UUID citaId) {
        return citaPort.buscarPorId(citaId);
    }
}
