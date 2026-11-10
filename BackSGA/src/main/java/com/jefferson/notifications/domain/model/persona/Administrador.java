package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public class Administrador extends Empleado {

    public Administrador(String nombre, LocalDate fechaNacimiento, String documento, String telefono,
                         String correo, String usuario, String contrasena, LocalDate fechaContratacion) {
        super(nombre, fechaNacimiento, documento, telefono, correo, usuario, contrasena, fechaContratacion);
    }

    @Override
    public Rol getRol() {
        return Rol.ADMINISTRADOR;
    }
}
