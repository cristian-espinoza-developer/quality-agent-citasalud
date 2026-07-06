package org.ups.citasalud.unit.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.ups.citasalud.application.event.CitaConfirmadaEvent;
import org.ups.citasalud.application.usecase.ReservarCitaService;
import org.ups.citasalud.domain.exception.FranjaOcupadaException;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.model.Paciente;
import org.ups.citasalud.domain.port.out.CitaPort;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;
import org.ups.citasalud.domain.port.out.MedicoPort;
import org.ups.citasalud.domain.port.out.PacientePort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservarCitaServiceTest {

    @Mock private CitaPort citaPort;
    @Mock private FranjaHorariaPort franjaHorariaPort;
    @Mock private PacientePort pacientePort;
    @Mock private MedicoPort medicoPort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ReservarCitaService service;

    private final UUID pacienteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID medicoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID franjaId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");

    @BeforeEach
    void setUp() {
        service = new ReservarCitaService(citaPort, franjaHorariaPort, pacientePort, medicoPort, eventPublisher);
    }

    @Nested
    class DadoPacienteValidoYFranjaDisponible {

        @Test
        void cuandoReservar_entoncesRetornaCitaConfirmadaYPublicaEvento() {
            Paciente paciente = new Paciente(pacienteId, "Juan", "Rodríguez", "+593987654321");
            FranjaHoraria franja = new FranjaHoraria(
                    franjaId, medicoId,
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.DISPONIBLE
            );
            Medico medico = new Medico(medicoId, "Ana", "García", "Medicina General", true);
            Cita citaEsperada = Cita.confirmar(pacienteId, franja);

            when(pacientePort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
            when(franjaHorariaPort.buscarConLockPesimista(franjaId)).thenReturn(Optional.of(franja));
            when(franjaHorariaPort.guardar(any())).thenReturn(franja);
            when(citaPort.guardar(any())).thenReturn(citaEsperada);
            when(medicoPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));

            Cita resultado = service.reservar(pacienteId, franjaId);

            assertEquals(EstadoCita.CONFIRMADA, resultado.getEstado());
            assertEquals(EstadoFranja.OCUPADA, franja.getEstado());
            ArgumentCaptor<CitaConfirmadaEvent> eventCaptor = ArgumentCaptor.forClass(CitaConfirmadaEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(citaEsperada.getId(), eventCaptor.getValue().getPayload().getCitaId());
        }
    }

    @Nested
    class DadaFranjaOcupada {

        @Test
        void cuandoReservar_entoncesLanzaFranjaOcupadaException() {
            Paciente paciente = new Paciente(pacienteId, "Juan", "Rodríguez", "+593987654321");
            FranjaHoraria franjaOcupada = new FranjaHoraria(
                    franjaId, medicoId,
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.OCUPADA
            );

            when(pacientePort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
            when(franjaHorariaPort.buscarConLockPesimista(franjaId)).thenReturn(Optional.of(franjaOcupada));

            assertThrows(FranjaOcupadaException.class, () -> service.reservar(pacienteId, franjaId));
        }
    }

    @Nested
    class DadoPacienteNoEncontrado {

        @Test
        void cuandoReservar_entoncesLanzaRecursoNoEncontradoException() {
            when(pacientePort.buscarPorId(pacienteId)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> service.reservar(pacienteId, franjaId));
        }
    }

    @Nested
    class DadaFranjaNoEncontrada {

        @Test
        void cuandoReservar_entoncesLanzaRecursoNoEncontradoException() {
            Paciente paciente = new Paciente(pacienteId, "Juan", "Rodríguez", "+593987654321");
            when(pacientePort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
            when(franjaHorariaPort.buscarConLockPesimista(franjaId)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> service.reservar(pacienteId, franjaId));
        }
    }

    @Nested
    class DadoCitaExistente {

        @Test
        void cuandoObtenerCita_entoncesRetornaOptionalConCita() {
            UUID citaId = UUID.randomUUID();
            FranjaHoraria franja = new FranjaHoraria(
                    franjaId, medicoId,
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    EstadoFranja.OCUPADA
            );
            Cita cita = Cita.confirmar(pacienteId, franja);
            when(citaPort.buscarPorId(citaId)).thenReturn(Optional.of(cita));

            Optional<Cita> resultado = service.obtenerCita(citaId);

            assertTrue(resultado.isPresent());
        }
    }

    @Nested
    class DadoCitaInexistente {

        @Test
        void cuandoObtenerCita_entoncesRetornaEmpty() {
            UUID citaId = UUID.randomUUID();
            when(citaPort.buscarPorId(citaId)).thenReturn(Optional.empty());

            Optional<Cita> resultado = service.obtenerCita(citaId);

            assertTrue(resultado.isEmpty());
        }
    }
}
