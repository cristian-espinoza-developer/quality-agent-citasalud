package org.ups.citasalud.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.entity.CitaJpaEntity;
import org.ups.citasalud.adapter.out.persistence.entity.FranjaHorariaJpaEntity;
import org.ups.citasalud.adapter.out.persistence.repository.CitaJpaRepository;
import org.ups.citasalud.adapter.out.persistence.repository.FranjaHorariaJpaRepository;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.port.out.CitaPort;

import java.util.Optional;
import java.util.UUID;

@Component
public class CitaPersistenceAdapter implements CitaPort {

    private final CitaJpaRepository citaJpaRepository;
    private final FranjaHorariaJpaRepository franjaJpaRepository;

    public CitaPersistenceAdapter(CitaJpaRepository citaJpaRepository,
                                   FranjaHorariaJpaRepository franjaJpaRepository) {
        this.citaJpaRepository = citaJpaRepository;
        this.franjaJpaRepository = franjaJpaRepository;
    }

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity entity = toEntity(cita);
        CitaJpaEntity saved = citaJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return citaJpaRepository.findById(id)
                .map(this::toDomain);
    }

    private CitaJpaEntity toEntity(Cita cita) {
        CitaJpaEntity entity = new CitaJpaEntity();
        entity.setId(cita.getId());
        entity.setPacienteId(cita.getPacienteId());
        entity.setFranjaHorariaId(cita.getFranjaHoraria().getId());
        entity.setEstado(cita.getEstado().name());
        entity.setFechaCreacion(cita.getFechaCreacion());
        return entity;
    }

    private Cita toDomain(CitaJpaEntity entity) {
        FranjaHorariaJpaEntity franjaEntity = franjaJpaRepository.findById(entity.getFranjaHorariaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Franja horaria no encontrada: " + entity.getFranjaHorariaId()));
        FranjaHoraria franja = toFranjaDomain(franjaEntity);
        return Cita.reconstruir(
                entity.getId(),
                entity.getPacienteId(),
                franja,
                EstadoCita.valueOf(entity.getEstado()),
                entity.getFechaCreacion()
        );
    }

    private FranjaHoraria toFranjaDomain(FranjaHorariaJpaEntity e) {
        return new FranjaHoraria(
                e.getId(), e.getMedicoId(), e.getFecha(),
                e.getHoraInicio(), e.getHoraFin(),
                EstadoFranja.valueOf(e.getEstado())
        );
    }
}
