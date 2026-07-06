package org.ups.citasalud.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.exception.RecursoNoEncontradoException;

import java.util.UUID;

/**
 * Extrae el pacienteId del contexto de autenticación.
 * MVP/dev: lee el UUID del header HTTP "X-Patient-Id".
 * Producción: reemplazar por extracción del claim "sub" del JWT Bearer.
 */
@Component
public class PatientIdentityExtractor {

    public static final String HEADER_NAME = "X-Patient-Id";

    public UUID extractPacienteId(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_NAME);
        if (headerValue == null || headerValue.isBlank()) {
            throw new RecursoNoEncontradoException(
                    "Header '" + HEADER_NAME + "' requerido para identificar al paciente");
        }
        try {
            return UUID.fromString(headerValue.trim());
        } catch (IllegalArgumentException e) {
            throw new RecursoNoEncontradoException(
                    "Header '" + HEADER_NAME + "' debe ser un UUID válido");
        }
    }
}
