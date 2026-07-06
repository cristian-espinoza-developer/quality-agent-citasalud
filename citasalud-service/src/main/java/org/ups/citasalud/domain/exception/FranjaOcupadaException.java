package org.ups.citasalud.domain.exception;

import java.util.UUID;

public class FranjaOcupadaException extends RuntimeException {

    private final UUID franjaId;

    public FranjaOcupadaException(UUID franjaId) {
        super("La franja horaria " + franjaId + " ya está ocupada");
        this.franjaId = franjaId;
    }

    public UUID getFranjaId() {
        return franjaId;
    }
}
