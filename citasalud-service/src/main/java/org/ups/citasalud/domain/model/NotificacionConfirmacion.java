package org.ups.citasalud.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class NotificacionConfirmacion {

    private final UUID citaId;
    private final String pacienteNombreCompleto;
    private final String telefonoWhatsApp;
    private final String medicoNombreCompleto;
    private final String medicoEspecialidad;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    public NotificacionConfirmacion(UUID citaId, String pacienteNombreCompleto,
                                     String telefonoWhatsApp, String medicoNombreCompleto,
                                     String medicoEspecialidad, LocalDate fecha,
                                     LocalTime horaInicio, LocalTime horaFin) {
        this.citaId = citaId;
        this.pacienteNombreCompleto = pacienteNombreCompleto;
        this.telefonoWhatsApp = telefonoWhatsApp;
        this.medicoNombreCompleto = medicoNombreCompleto;
        this.medicoEspecialidad = medicoEspecialidad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public UUID getCitaId() { return citaId; }
    public String getPacienteNombreCompleto() { return pacienteNombreCompleto; }
    public String getTelefonoWhatsApp() { return telefonoWhatsApp; }
    public String getMedicoNombreCompleto() { return medicoNombreCompleto; }
    public String getMedicoEspecialidad() { return medicoEspecialidad; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
}
