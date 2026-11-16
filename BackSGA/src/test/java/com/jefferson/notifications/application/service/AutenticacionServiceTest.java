package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.exception.NoAutorizadoException;
import com.jefferson.notifications.application.port.in.AutenticacionUseCase.SesionIniciada;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutenticacionServiceTest {

    private AutenticacionService servicio;
    private Alojamiento alojamiento;

    @BeforeEach
    void preparar() {
        AlojamientoRepositoryPort repositorio = mock(AlojamientoRepositoryPort.class);
        alojamiento = AlojamientoDePrueba.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
        servicio = new AutenticacionService(repositorio);
    }

    @Test
    void elLoginDevuelveUnTokenQueIdentificaAlUsuario() {
        SesionIniciada sesion = servicio.iniciarSesion("admin", "admin123");
        assertNotNull(sesion.token());
        assertEquals(Rol.ADMINISTRADOR, servicio.usuarioDeLaSesion(sesion.token()).getRol());
    }

    @Test
    void credencialesIncorrectasSonUn401() {
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion("admin", "mala"));
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion("nadie", "x"));
    }

    @Test
    void unEmpleadoDesvinculadoNoEntra() {
        alojamiento.desvincularEmpleado("2");
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion("recepcion", "recepcion123"));
    }

    @Test
    void unTokenInventadoNoEsValido() {
        assertThrows(NoAutorizadoException.class, () -> servicio.usuarioDeLaSesion("inventado"));
        assertThrows(NoAutorizadoException.class, () -> servicio.usuarioDeLaSesion(null));
    }

    @Test
    void registraHuespedesSinRepetirUsuario() {
        servicio.registrarHuesped(new Huesped("Ana", LocalDate.of(2000, 1, 1), "555", "300", "a@x.co", "ana", "123456"));
        assertEquals(Rol.HUESPED, servicio.iniciarSesion("ana", "123456").usuario().getRol());
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarHuesped(
                new Huesped("Otra", LocalDate.of(2000, 1, 1), "556", "300", "o@x.co", "ana", "123456")));
    }

    @Test
    void cambiaLaContrasenaSoloConLaActualCorrecta() {
        String token = servicio.iniciarSesion("laura", "laura123").token();
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarContrasena(token, "mala", "nueva123"));
        servicio.cambiarContrasena(token, "laura123", "nueva123");
        assertNotNull(servicio.iniciarSesion("laura", "nueva123"));
    }
}
