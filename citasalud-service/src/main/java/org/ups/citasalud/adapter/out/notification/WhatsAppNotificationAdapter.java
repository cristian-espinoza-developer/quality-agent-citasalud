package org.ups.citasalud.adapter.out.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.ups.citasalud.application.event.CitaConfirmadaEvent;
import org.ups.citasalud.domain.model.NotificacionConfirmacion;
import org.ups.citasalud.domain.port.out.NotificacionPort;

@Component
public class WhatsAppNotificationAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationAdapter.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCitaConfirmada(CitaConfirmadaEvent event) {
        notificarConfirmacion(event.getPayload());
    }

    @Override
    public void notificarConfirmacion(NotificacionConfirmacion notificacion) {
        log.info("Enviando notificación WhatsApp a {} para cita {} el {} a las {}",
                notificacion.getTelefonoWhatsApp(),
                notificacion.getCitaId(),
                notificacion.getFecha(),
                notificacion.getHoraInicio());

        // MVP stub — producción: integrar WhatsApp Business API con retry exponencial
        log.info("Notificación enviada: Estimado/a {}, su cita con {} ({}) queda confirmada para el {} de {} a {}.",
                notificacion.getPacienteNombreCompleto(),
                notificacion.getMedicoNombreCompleto(),
                notificacion.getMedicoEspecialidad(),
                notificacion.getFecha(),
                notificacion.getHoraInicio(),
                notificacion.getHoraFin());
    }
}
