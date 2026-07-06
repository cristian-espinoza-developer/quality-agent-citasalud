package org.ups.citasalud.unit.domain.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoNoEncontradoExceptionTest {

    @Nested
    class DadoMensaje {

        @Test
        void cuandoInstanciar_entoncesGetMensajeYGetMessageRetornanElMensaje() {
            String mensaje = "Paciente no encontrado: 123";
            RecursoNoEncontradoException ex = new RecursoNoEncontradoException(mensaje);

            assertEquals(mensaje, ex.getMensaje());
            assertEquals(mensaje, ex.getMessage());
        }
    }
}
