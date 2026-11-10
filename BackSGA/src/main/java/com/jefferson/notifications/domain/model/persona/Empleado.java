package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public class Empleado extends Usuario {

    protected LocalDate fechaContratacion;
    protected boolean activo = true;

    public Empleado(String nombre, LocalDate fechaNacimiento, String documento, String telefono,
                    String correo, String usuario, String contrasena, LocalDate fechaContratacion) {
        super(nombre, fechaNacimiento, documento, telefono, correo, usuario, contrasena);
        this.fechaContratacion = fechaContratacion;
    }


    public void desvincular() {
        this.activo = false;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }
}
