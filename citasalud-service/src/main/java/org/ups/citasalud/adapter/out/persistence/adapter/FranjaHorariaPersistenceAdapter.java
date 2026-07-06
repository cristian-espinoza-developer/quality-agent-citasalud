package org.ups.citasalud.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.entity.FranjaHorariaJpaEntity;
import org.ups.citasalud.adapter.out.persistence.repository.FranjaHorariaJpaRepository;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FranjaHorariaPersistenceAdapter implements FranjaHorariaPort {

    private final FranjaHorariaJpaRepository repository;

    public FranjaHorariaPersistenceAdapter(FranjaHorariaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FranjaHoraria> buscarConLockPesimista(UUID id) {
        return repository.findByIdForUpdate(id).map(this::toDomain);
    }

    @Override
    public FranjaHoraria guardar(FranjaHoraria franjaHoraria) {
        // findById returns the managed entity from 1st-level cache when in same transaction,
        // preventing NonUniqueObjectException if the entity was already loaded with a lock.
        FranjaHorariaJpaEntity entity = repository.findById(franjaHoraria.getId())
                .orElseGet(FranjaHorariaJpaEntity::new);
        entity.setId(franjaHoraria.getId());
        entity.setMedicoId(franjaHoraria.getMedicoId());
        entity.setFecha(franjaHoraria.getFecha());
        entity.setHoraInicio(franjaHoraria.getHoraInicio());
        entity.setHoraFin(franjaHoraria.getHoraFin());
        entity.setEstado(franjaHoraria.getEstado().name());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<FranjaHoraria> buscarPorMedicoYFecha(UUID medicoId, LocalDate fecha) {
        return repository.findByMedicoIdAndFecha(medicoId, fecha)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private FranjaHoraria toDomain(FranjaHorariaJpaEntity e) {
        return new FranjaHoraria(
                e.getId(), e.getMedicoId(), e.getFecha(),
                e.getHoraInicio(), e.getHoraFin(),
                EstadoFranja.valueOf(e.getEstado())
        );
    }

}
