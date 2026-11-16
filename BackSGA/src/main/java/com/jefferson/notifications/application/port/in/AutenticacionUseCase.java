package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Usuario;

public interface AutenticacionUseCase {

    SesionIniciada iniciarSesion(String usuario, String contrasena);

    Huesped registrarHuesped(Huesped huesped);

    void cambiarContrasena(String token, String actual, String nueva);

    Usuario usuarioDeLaSesion(String token);

    record SesionIniciada(String token, Usuario usuario) {
    }
}