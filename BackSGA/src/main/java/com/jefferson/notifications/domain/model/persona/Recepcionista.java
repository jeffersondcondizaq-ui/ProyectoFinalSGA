package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public abstract class Recepcionista extends Empleado {

    public Recepcionista(String nombre, LocalDate fechaNacimiento, String documento, String telefono,
                         String correo, String usuario, String contrasena, LocalDate fechaContratacion) {
        super(nombre, fechaNacimiento, documento, telefono, correo, usuario, contrasena, fechaContratacion);
    }

    @Override
    public Rol getRol() {
        return Rol.RECEPCIONISTA;
    }
}
