package org.ups.citasalud.domain.model;

import java.util.UUID;

public class Medico {

    private final UUID id;
    private final String nombre;
    private final String apellido;
    private final String especialidad;
    private final boolean disponibleOnline;

    public Medico(UUID id, String nombre, String apellido, String especialidad, boolean disponibleOnline) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidad = especialidad;
        this.disponibleOnline = disponibleOnline;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEspecialidad() { return especialidad; }
    public boolean isDisponibleOnline() { return disponibleOnline; }
}
