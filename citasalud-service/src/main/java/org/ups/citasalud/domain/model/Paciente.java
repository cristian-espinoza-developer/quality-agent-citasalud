package org.ups.citasalud.domain.model;

import java.util.UUID;

public class Paciente {

    private final UUID id;
    private final String nombre;
    private final String apellido;
    private final String telefonoWhatsApp;

    public Paciente(UUID id, String nombre, String apellido, String telefonoWhatsApp) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefonoWhatsApp = telefonoWhatsApp;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefonoWhatsApp() { return telefonoWhatsApp; }
}
