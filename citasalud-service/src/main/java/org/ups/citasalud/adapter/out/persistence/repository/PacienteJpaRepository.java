package org.ups.citasalud.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.adapter.out.persistence.entity.PacienteJpaEntity;

import java.util.UUID;

public interface PacienteJpaRepository extends JpaRepository<PacienteJpaEntity, UUID> {
}
