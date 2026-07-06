package org.ups.citasalud.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    private final String mensaje;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
