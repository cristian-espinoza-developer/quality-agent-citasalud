package org.ups.citasalud.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.entity.PacienteJpaEntity;
import org.ups.citasalud.adapter.out.persistence.repository.PacienteJpaRepository;
import org.ups.citasalud.domain.model.Paciente;
import org.ups.citasalud.domain.port.out.PacientePort;

import java.util.Optional;
import java.util.UUID;

@Component
public class PacientePersistenceAdapter implements PacientePort {

    private final PacienteJpaRepository repository;

    public PacientePersistenceAdapter(PacienteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Paciente> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private Paciente toDomain(PacienteJpaEntity e) {
        return new Paciente(e.getId(), e.getNombre(), e.getApellido(), e.getTelefonoWhatsApp());
    }
}
