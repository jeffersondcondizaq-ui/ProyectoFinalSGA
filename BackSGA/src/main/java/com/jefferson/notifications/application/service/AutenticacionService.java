package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.exception.NoAutorizadoException;
import com.jefferson.notifications.application.port.in.AutenticacionUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Usuario;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones con token: al iniciar sesión se genera un token aleatorio (UUID) y se guarda
 * en memoria junto con su usuario. El front lo envía en la cabecera Authorization: Bearer ...
 */
public class AutenticacionService extends ServicioBase implements AutenticacionUseCase {

    private final Map<String, Usuario> sesiones = new ConcurrentHashMap<>();

    public AutenticacionService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public SesionIniciada iniciarSesion(String usuario, String contrasena) {
        Usuario encontrado;
        try {
            encontrado = ejecutar(alojamiento -> alojamiento.iniciarSesion(usuario, contrasena));
        } catch (ReglaNegocioException e) {
            // Credenciales malas o empleado desvinculado: es un 401, no un 409
            throw new NoAutorizadoException(e.getMessage());
        }
        String token = UUID.randomUUID().toString();
        sesiones.put(token, encontrado);
        return new SesionIniciada(token, encontrado);
    }

    @Override
    public Huesped registrarHuesped(Huesped huesped) {
        ejecutar(alojamiento -> alojamiento.registrarHuesped(huesped));
        return huesped;
    }

    @Override
    public void cambiarContrasena(String token, String actual, String nueva) {
        Usuario usuario = usuarioDeLaSesion(token);
        if (!usuario.cambiarContrasena(actual, nueva)) {
            throw new ReglaNegocioException("La contraseña actual no es correcta");
        }
    }

    @Override
    public Usuario usuarioDeLaSesion(String token) {
        Usuario usuario = token == null ? null : sesiones.get(token);
        if (usuario == null) {
            throw new NoAutorizadoException("La sesión no es válida o expiró. Inicia sesión de nuevo");
        }
        return usuario;
    }
}
