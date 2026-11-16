package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.exception.AccesoDenegadoException;
import com.jefferson.notifications.application.exception.NoAutorizadoException;
import com.jefferson.notifications.application.port.in.AutenticacionUseCase;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Autorización por rol: lee el token de la petición actual y comprueba quién la hace.
 * Los controladores la usan al inicio de cada endpoint protegido.
 */
@Component
public class SesionWeb {

    private final AutenticacionUseCase autenticacion;
    private final HttpServletRequest peticion;

    public SesionWeb(AutenticacionUseCase autenticacion, HttpServletRequest peticion) {
        this.autenticacion = autenticacion;
        this.peticion = peticion;
    }

    /** Usuario con sesión; 401 si no hay token válido. */
    public Usuario usuario() {
        String token = CabeceraAutorizacion.token(peticion.getHeader(HttpHeaders.AUTHORIZATION));
        return autenticacion.usuarioDeLaSesion(token);
    }

    /** Usuario con sesión o null (para endpoints públicos que muestran más a un empleado). */
    public Usuario usuarioOpcional() {
        try {
            return usuario();
        } catch (NoAutorizadoException sinSesion) {
            return null;
        }
    }

    /** Usuario con sesión y uno de los roles indicados; 401 sin sesión, 403 con otro rol. */
    public Usuario exigirRol(Rol... roles) {
        Usuario usuario = usuario();
        if (!Arrays.asList(roles).contains(usuario.getRol())) {
            throw new AccesoDenegadoException("No tienes permiso para realizar esta operación");
        }
        return usuario;
    }

    /** Recepción y administración: cualquier empleado activo. */
    public Usuario exigirEmpleado() {
        return exigirRol(Rol.RECEPCIONISTA, Rol.ADMINISTRADOR);
    }

    /** Un huésped solo puede ver y operar sus propias reservas; los empleados, todas. */
    public void exigirAccesoA(Reserva reserva) {
        Usuario usuario = usuario();
        if (usuario.getRol() == Rol.HUESPED
                && !reserva.getTitular().getDocumento().equals(usuario.getDocumento())) {
            throw new AccesoDenegadoException("La reserva " + reserva.getCodigo() + " no es tuya");
        }
    }
}
