package org.ups.citasalud.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.adapter.out.persistence.entity.CitaJpaEntity;

import java.util.UUID;

public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, UUID> {
}
