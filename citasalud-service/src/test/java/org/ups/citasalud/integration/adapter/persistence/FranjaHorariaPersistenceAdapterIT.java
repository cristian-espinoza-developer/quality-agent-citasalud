package org.ups.citasalud.integration.adapter.persistence;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.ups.citasalud.adapter.out.persistence.adapter.FranjaHorariaPersistenceAdapter;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import(FranjaHorariaPersistenceAdapter.class)
class FranjaHorariaPersistenceAdapterIT {

    @Autowired
    private FranjaHorariaPersistenceAdapter adapter;

    private final UUID franjaDisponibleId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
    private final UUID medicoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Nested
    class DadaFranjaNueva {

        @Test
        void cuandoGuardar_entoncesSeInsertaEnBD() {
            UUID nuevaFranjaId = UUID.randomUUID();
            FranjaHoraria nueva = new FranjaHoraria(
                    nuevaFranjaId, medicoId,
                    LocalDate.of(2026, 9, 1),
                    LocalTime.of(11, 0), LocalTime.of(11, 30),
                    EstadoFranja.DISPONIBLE
            );

            FranjaHoraria guardada = adapter.guardar(nueva);

            assertNotNull(guardada);
            assertEquals(nuevaFranjaId, guardada.getId());
            assertEquals(EstadoFranja.DISPONIBLE, guardada.getEstado());
        }
    }

    @Nested
    class DadaFranjaDisponible {

        @Test
        void cuandoBuscarConLockPesimista_entoncesRetornaFranjaYPermiteCambioEstado() {
            Optional<FranjaHoraria> resultado = adapter.buscarConLockPesimista(franjaDisponibleId);

            assertTrue(resultado.isPresent());
            assertEquals(EstadoFranja.DISPONIBLE, resultado.get().getEstado());

            FranjaHoraria franja = resultado.get();
            franja.reservar();
            adapter.guardar(franja);

            Optional<FranjaHoraria> actualizada = adapter.buscarConLockPesimista(franjaDisponibleId);
            assertTrue(actualizada.isPresent());
            assertEquals(EstadoFranja.OCUPADA, actualizada.get().getEstado());
        }
    }
}
