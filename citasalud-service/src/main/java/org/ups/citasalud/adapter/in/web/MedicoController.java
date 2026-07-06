package org.ups.citasalud.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.adapter.in.web.mapper.MedicoWebMapper;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.openapi.api.DisponibilidadApi;
import org.ups.citasalud.openapi.api.MedicosApi;
import org.ups.citasalud.openapi.model.DisponibilidadMedicoResponse;
import org.ups.citasalud.openapi.model.ListaMedicosResponse;
import org.ups.citasalud.openapi.model.MedicoResumen;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class MedicoController implements MedicosApi, DisponibilidadApi {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final MedicoWebMapper medicoWebMapper;

    public MedicoController(ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                             MedicoWebMapper medicoWebMapper) {
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.medicoWebMapper = medicoWebMapper;
    }

    @Override
    public ResponseEntity<ListaMedicosResponse> listarMedicosDisponibles(String especialidad) {
        List<Medico> medicos = consultarDisponibilidadUseCase.listarMedicosDisponibles(especialidad);
        List<MedicoResumen> resumenes = medicos.stream()
                .map(medicoWebMapper::toResumen)
                .collect(Collectors.toList());
        ListaMedicosResponse response = new ListaMedicosResponse();
        response.setMedicos(resumenes);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<DisponibilidadMedicoResponse> consultarDisponibilidad(UUID medicoId, LocalDate fecha) {
        List<FranjaHoraria> franjas = consultarDisponibilidadUseCase.consultarDisponibilidad(medicoId, fecha);
        return ResponseEntity.ok(medicoWebMapper.toDisponibilidadResponse(medicoId, fecha, franjas));
    }
}
