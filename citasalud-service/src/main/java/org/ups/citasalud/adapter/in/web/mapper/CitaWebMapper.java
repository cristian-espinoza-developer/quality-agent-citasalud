package org.ups.citasalud.adapter.in.web.mapper;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.openapi.model.CitaConfirmadaResponse;
import org.ups.citasalud.openapi.model.MedicoResumen;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class CitaWebMapper {

    public CitaConfirmadaResponse toResponse(Cita cita, Medico medico) {
        CitaConfirmadaResponse response = new CitaConfirmadaResponse();
        response.setCitaId(cita.getId());
        response.setEstado(CitaConfirmadaResponse.EstadoEnum.valueOf(cita.getEstado().name()));
        response.setFecha(cita.getFranjaHoraria().getFecha());
        response.setHoraInicio(cita.getFranjaHoraria().getHoraInicio().toString());
        response.setHoraFin(cita.getFranjaHoraria().getHoraFin().toString());
        response.setFechaCreacion(cita.getFechaCreacion().atOffset(ZoneOffset.UTC));
        response.setMedico(toMedicoResumen(medico));
        return response;
    }

    private MedicoResumen toMedicoResumen(Medico medico) {
        MedicoResumen resumen = new MedicoResumen();
        resumen.setId(medico.getId());
        resumen.setNombre(medico.getNombre());
        resumen.setApellido(medico.getApellido());
        resumen.setEspecialidad(medico.getEspecialidad());
        return resumen;
    }
}
