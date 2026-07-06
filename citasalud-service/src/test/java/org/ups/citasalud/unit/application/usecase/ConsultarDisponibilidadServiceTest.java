package org.ups.citasalud.unit.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.application.usecase.ConsultarDisponibilidadService;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;
import org.ups.citasalud.domain.port.out.MedicoPort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadServiceTest {

    @Mock private MedicoPort medicoPort;
    @Mock private FranjaHorariaPort franjaHorariaPort;

    private ConsultarDisponibilidadService service;

    private final UUID medicoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final LocalDate fecha = LocalDate.of(2026, 7, 15);

    @BeforeEach
    void setUp() {
        service = new ConsultarDisponibilidadService(medicoPort, franjaHorariaPort);
    }

    @Nested
    class DadoMedicoConFranjasDisponibles {

        @Test
        void cuandoConsultarDisponibilidad_entoncesRetornaFranjasConEstado() {
            Medico medico = new Medico(medicoId, "Ana", "García", "Medicina General", true);
            List<FranjaHoraria> franjas = List.of(
                    new FranjaHoraria(UUID.randomUUID(), medicoId, fecha,
                            LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoFranja.DISPONIBLE),
                    new FranjaHoraria(UUID.randomUUID(), medicoId, fecha,
                            LocalTime.of(10, 0), LocalTime.of(10, 30), EstadoFranja.OCUPADA)
            );

            when(medicoPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
            when(franjaHorariaPort.buscarPorMedicoYFecha(medicoId, fecha)).thenReturn(franjas);

            List<FranjaHoraria> resultado = service.consultarDisponibilidad(medicoId, fecha);

            assertEquals(2, resultado.size());
            assertEquals(EstadoFranja.DISPONIBLE, resultado.get(0).getEstado());
        }
    }

    @Nested
    class DadoMedicoSinFranjasParaFecha {

        @Test
        void cuandoConsultar_entoncesRetornaListaVacia() {
            Medico medico = new Medico(medicoId, "Ana", "García", "Medicina General", true);

            when(medicoPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
            when(franjaHorariaPort.buscarPorMedicoYFecha(medicoId, fecha)).thenReturn(List.of());

            List<FranjaHoraria> resultado = service.consultarDisponibilidad(medicoId, fecha);

            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    class DadoMedicoNoExistente {

        @Test
        void cuandoConsultarDisponibilidad_entoncesLanzaRecursoNoEncontradoException() {
            when(medicoPort.buscarPorId(medicoId)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class,
                    () -> service.consultarDisponibilidad(medicoId, fecha));
        }
    }
}
