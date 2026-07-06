package org.ups.citasalud.unit.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CitaTest {

    @Nested
    class DadoCitaCreada {

        @Test
        void cuandoConsultarEstado_entoncesRetornaConfirmada() {
            FranjaHoraria franja = new FranjaHoraria(
                    UUID.randomUUID(), UUID.randomUUID(),
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.DISPONIBLE
            );

            Cita cita = Cita.confirmar(UUID.randomUUID(), franja);

            assertNotNull(cita.getId());
            assertEquals(EstadoCita.CONFIRMADA, cita.getEstado());
            assertNotNull(cita.getFechaCreacion());
        }
    }
}
