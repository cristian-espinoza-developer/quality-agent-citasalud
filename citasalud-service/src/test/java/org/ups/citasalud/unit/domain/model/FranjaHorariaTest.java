package org.ups.citasalud.unit.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ups.citasalud.domain.exception.FranjaOcupadaException;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FranjaHorariaTest {

    @Nested
    class DadaFranjaDisponible {

        @Test
        void cuandoReservar_entoncesEstadoCambiaAOcupada() {
            FranjaHoraria franja = new FranjaHoraria(
                    UUID.randomUUID(), UUID.randomUUID(),
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.DISPONIBLE
            );

            franja.reservar();

            assertEquals(EstadoFranja.OCUPADA, franja.getEstado());
        }
    }

    @Nested
    class DadaFranjaOcupada {

        @Test
        void cuandoReservar_entoncesLanzaFranjaOcupadaException() {
            UUID franjaId = UUID.randomUUID();
            FranjaHoraria franja = new FranjaHoraria(
                    franjaId, UUID.randomUUID(),
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.OCUPADA
            );

            FranjaOcupadaException ex = assertThrows(FranjaOcupadaException.class, franja::reservar);
            assertEquals(franjaId, ex.getFranjaId());
        }
    }
}
