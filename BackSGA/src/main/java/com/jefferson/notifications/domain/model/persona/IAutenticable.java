package com.jefferson.notifications.domain.model.persona;

public interface IAutenticable {

    boolean autenticar(String usuario, String contrasena);

    Rol getRol();
}
