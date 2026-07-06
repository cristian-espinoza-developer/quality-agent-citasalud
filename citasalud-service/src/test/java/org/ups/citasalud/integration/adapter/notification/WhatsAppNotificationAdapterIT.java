package org.ups.citasalud.integration.adapter.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.ups.citasalud.adapter.out.notification.WhatsAppNotificationAdapter;
import org.ups.citasalud.domain.model.NotificacionConfirmacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class WhatsAppNotificationAdapterIT {

    @Autowired
    private WhatsAppNotificationAdapter adapter;

    @Nested
    class DadoCitaConfirmadaEvent {

        @Test
        void cuandoNotificarConfirmacion_entoncesLogNotificacionSinExcepcion() {
            NotificacionConfirmacion notificacion = new NotificacionConfirmacion(
                    UUID.randomUUID(),
                    "Juan Rodríguez",
                    "+593987654321",
                    "Ana García",
                    "Medicina General",
                    LocalDate.of(2026, 7, 15),
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 30)
            );

            assertDoesNotThrow(() -> adapter.notificarConfirmacion(notificacion));
        }
    }
}
