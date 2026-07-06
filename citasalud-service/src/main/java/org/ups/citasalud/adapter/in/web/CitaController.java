package org.ups.citasalud.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.adapter.in.web.mapper.CitaWebMapper;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.out.MedicoPort;
import org.ups.citasalud.openapi.api.CitasApi;
import org.ups.citasalud.openapi.model.CitaConfirmadaResponse;
import org.ups.citasalud.openapi.model.ReservarCitaRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CitaController implements CitasApi {

    private final ReservarCitaUseCase reservarCitaUseCase;
    private final MedicoPort medicoPort;
    private final CitaWebMapper citaWebMapper;
    private final PatientIdentityExtractor patientIdentityExtractor;
    private final HttpServletRequest httpServletRequest;

    public CitaController(ReservarCitaUseCase reservarCitaUseCase,
                          MedicoPort medicoPort,
                          CitaWebMapper citaWebMapper,
                          PatientIdentityExtractor patientIdentityExtractor,
                          HttpServletRequest httpServletRequest) {
        this.reservarCitaUseCase = reservarCitaUseCase;
        this.medicoPort = medicoPort;
        this.citaWebMapper = citaWebMapper;
        this.patientIdentityExtractor = patientIdentityExtractor;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public ResponseEntity<CitaConfirmadaResponse> reservarCita(ReservarCitaRequest request) {
        UUID pacienteId = patientIdentityExtractor.extractPacienteId(httpServletRequest);
        Cita cita = reservarCitaUseCase.reservar(pacienteId, request.getFranjaHorariaId());
        Medico medico = medicoPort.buscarPorId(cita.getFranjaHoraria().getMedicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));
        return ResponseEntity.status(HttpStatus.CREATED).body(citaWebMapper.toResponse(cita, medico));
    }

    @Override
    public ResponseEntity<CitaConfirmadaResponse> obtenerCita(UUID citaId) {
        Cita cita = reservarCitaUseCase.obtenerCita(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        Medico medico = medicoPort.buscarPorId(cita.getFranjaHoraria().getMedicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));
        return ResponseEntity.ok(citaWebMapper.toResponse(cita, medico));
    }
}
