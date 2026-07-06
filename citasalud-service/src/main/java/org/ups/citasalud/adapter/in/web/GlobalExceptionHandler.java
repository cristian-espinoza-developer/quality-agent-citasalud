package org.ups.citasalud.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.citasalud.domain.exception.FranjaOcupadaException;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;
import org.ups.citasalud.openapi.model.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FranjaOcupadaException.class)
    public ResponseEntity<ErrorResponse> handleFranjaOcupada(FranjaOcupadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse("FRANJA_OCUPADA",
                        "La franja horaria seleccionada ya no está disponible. Por favor, elija otra franja."));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse("NO_ENCONTRADO", ex.getMensaje()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Solicitud inválida");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse("SOLICITUD_INVALIDA", mensaje));
    }

    private ErrorResponse errorResponse(String codigo, String mensaje) {
        ErrorResponse response = new ErrorResponse();
        response.setCodigo(codigo);
        response.setMensaje(mensaje);
        response.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return response;
    }
}
