package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public class Huesped extends Usuario {
    private String foto; // ruta de la imagen (opcional)

    public Huesped(String nombre, LocalDate fechaNacimiento, String documento, String telefono,
                   String correo, String usuario, String contrasena) {
        super(nombre, fechaNacimiento, documento, telefono, correo, usuario, contrasena);
    }

    public void actualizarPerfil(String telefono, String correo) {
        if (telefono == null || telefono.isEmpty()) {
            return;
        }
        if (correo == null || !correo.contains("@")) {
            return;
        }
        this.telefono = telefono;
        this.correo = correo;
    }

    @Override
    public Rol getRol() {
        return Rol.HUESPED;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String ruta) {
        this.foto = ruta;
    }
}
