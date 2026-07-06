package org.ups.citasalud.integration.adapter.persistence;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.ups.citasalud.adapter.out.persistence.adapter.CitaPersistenceAdapter;
import org.ups.citasalud.adapter.out.persistence.repository.CitaJpaRepository;
import org.ups.citasalud.adapter.out.persistence.repository.FranjaHorariaJpaRepository;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import(CitaPersistenceAdapter.class)
class CitaPersistenceAdapterIT {

    @Autowired
    private CitaPersistenceAdapter citaPersistenceAdapter;

    @Autowired
    private FranjaHorariaJpaRepository franjaJpaRepository;

    private final UUID medicoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID franjaId = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
    private final UUID pacienteId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Nested
    class DadaCitaValida {

        @Test
        void cuandoGuardar_entoncesPersisteEnH2YRecuperaPorId() {
            FranjaHoraria franja = new FranjaHoraria(
                    franjaId, medicoId,
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 30), LocalTime.of(10, 0),
                    EstadoFranja.DISPONIBLE
            );

            Cita cita = Cita.confirmar(pacienteId, franja);
            Cita guardada = citaPersistenceAdapter.guardar(cita);

            Optional<Cita> recuperada = citaPersistenceAdapter.buscarPorId(guardada.getId());

            assertTrue(recuperada.isPresent());
            assertEquals(guardada.getId(), recuperada.get().getId());
            assertEquals(pacienteId, recuperada.get().getPacienteId());
        }
    }
}
