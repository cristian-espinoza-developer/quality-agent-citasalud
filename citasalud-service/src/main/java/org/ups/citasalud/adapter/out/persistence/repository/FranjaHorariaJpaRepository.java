package org.ups.citasalud.adapter.out.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ups.citasalud.adapter.out.persistence.entity.FranjaHorariaJpaEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FranjaHorariaJpaRepository extends JpaRepository<FranjaHorariaJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FranjaHorariaJpaEntity f WHERE f.id = :id")
    Optional<FranjaHorariaJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    List<FranjaHorariaJpaEntity> findByMedicoIdAndFecha(UUID medicoId, LocalDate fecha);
}
