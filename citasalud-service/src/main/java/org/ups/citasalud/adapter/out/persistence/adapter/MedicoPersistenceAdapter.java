package org.ups.citasalud.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.entity.MedicoJpaEntity;
import org.ups.citasalud.adapter.out.persistence.repository.MedicoJpaRepository;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.out.MedicoPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MedicoPersistenceAdapter implements MedicoPort {

    private final MedicoJpaRepository repository;

    public MedicoPersistenceAdapter(MedicoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Medico> listarDisponiblesOnline(String especialidad) {
        List<MedicoJpaEntity> entities = (especialidad == null || especialidad.isBlank())
                ? repository.findByDisponibleOnlineTrue()
                : repository.findByDisponibleOnlineTrueAndEspecialidadContainingIgnoreCase(especialidad);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Medico> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private Medico toDomain(MedicoJpaEntity e) {
        return new Medico(e.getId(), e.getNombre(), e.getApellido(), e.getEspecialidad(), e.isDisponibleOnline());
    }
}
