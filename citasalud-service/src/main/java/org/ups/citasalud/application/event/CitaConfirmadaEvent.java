package org.ups.citasalud.application.event;

import org.springframework.context.ApplicationEvent;
import org.ups.citasalud.domain.model.NotificacionConfirmacion;

public class CitaConfirmadaEvent extends ApplicationEvent {

    private final NotificacionConfirmacion payload;

    public CitaConfirmadaEvent(Object source, NotificacionConfirmacion payload) {
        super(source);
        this.payload = payload;
    }

    public NotificacionConfirmacion getPayload() {
        return payload;
    }
}
