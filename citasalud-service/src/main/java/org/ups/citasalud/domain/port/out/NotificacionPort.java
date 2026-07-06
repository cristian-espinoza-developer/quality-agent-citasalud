package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.NotificacionConfirmacion;

public interface NotificacionPort {

    void notificarConfirmacion(NotificacionConfirmacion notificacion);
}
