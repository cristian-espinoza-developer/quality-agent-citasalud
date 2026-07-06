package org.ups.citasalud.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.adapter.out.persistence.entity.MedicoJpaEntity;

import java.util.List;
import java.util.UUID;

public interface MedicoJpaRepository extends JpaRepository<MedicoJpaEntity, UUID> {

    List<MedicoJpaEntity> findByDisponibleOnlineTrue();

    List<MedicoJpaEntity> findByDisponibleOnlineTrueAndEspecialidadContainingIgnoreCase(String especialidad);
}
