package org.ups.citasalud.adapter.in.web.mapper;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.openapi.model.DisponibilidadMedicoResponse;
import org.ups.citasalud.openapi.model.FranjaHorariaInfo;
import org.ups.citasalud.openapi.model.MedicoResumen;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MedicoWebMapper {

    public MedicoResumen toResumen(Medico medico) {
        MedicoResumen resumen = new MedicoResumen();
        resumen.setId(medico.getId());
        resumen.setNombre(medico.getNombre());
        resumen.setApellido(medico.getApellido());
        resumen.setEspecialidad(medico.getEspecialidad());
        return resumen;
    }

    public DisponibilidadMedicoResponse toDisponibilidadResponse(UUID medicoId, LocalDate fecha,
                                                                   List<FranjaHoraria> franjas) {
        DisponibilidadMedicoResponse response = new DisponibilidadMedicoResponse();
        response.setMedicoId(medicoId);
        response.setFecha(fecha);
        response.setFranjas(franjas.stream().map(this::toFranjaInfo).collect(Collectors.toList()));
        return response;
    }

    private FranjaHorariaInfo toFranjaInfo(FranjaHoraria franja) {
        FranjaHorariaInfo info = new FranjaHorariaInfo();
        info.setId(franja.getId());
        info.setHoraInicio(franja.getHoraInicio().toString());
        info.setHoraFin(franja.getHoraFin().toString());
        info.setDisponible(franja.getEstado() == EstadoFranja.DISPONIBLE);
        return info;
    }
}
