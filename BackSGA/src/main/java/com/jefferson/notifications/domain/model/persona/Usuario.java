package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public abstract class Usuario extends Persona implements IAutenticable{
    protected String documento;
    protected String telefono;
    protected String correo;
    protected String usuario;
    protected String contrasena;
    protected Rol rol;

    public Usuario(String nombre, LocalDate fechaNacimiento, String documento, String telefono,
                   String correo, String usuario, String contrasena) {
        super(nombre, fechaNacimiento);
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public Rol getRol() {
        return rol;
    }

    @Override
    public boolean autenticar(String usuario, String contrasena) {
        if (this.usuario.equals(usuario) && this.contrasena.equals(contrasena)) {
            return true;
        }
        return false;
    }

    public boolean cambiarContrasena(String actual, String nueva) {
        if (!this.contrasena.equals(actual)) {
            return false;
        }
        if (nueva == null || nueva.isEmpty()) {
            return false;
        }
        this.contrasena = nueva;
        return true;
    }


}
