package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.exception.AccesoDenegadoException;
import com.jefferson.notifications.application.exception.NoAutorizadoException;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.application.service.AutenticacionService;
import com.jefferson.notifications.application.service.ReservaService;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.infrastructure.adapter.out.persistence.DatosSemilla;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Autorización por rol: 401 sin sesión, 403 con otro rol, y el huésped solo accede a sus reservas. */
class SesionWebTest {

    private AutenticacionService autenticacion;
    private HttpServletRequest peticion;
    private SesionWeb sesion;
    private Alojamiento alojamiento;

    @BeforeEach
    void preparar() {
        AlojamientoRepositoryPort repositorio = mock(AlojamientoRepositoryPort.class);
        alojamiento = DatosSemilla.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
        autenticacion = new AutenticacionService(repositorio);
        peticion = mock(HttpServletRequest.class);
        sesion = new SesionWeb(autenticacion, peticion);
    }

    /** Simula que la petición llega con el token de ese usuario. */
    private void conSesionDe(String usuario, String contrasena) {
        String token = autenticacion.iniciarSesion(usuario, contrasena).token();
        when(peticion.getHeader("Authorization")).thenReturn("Bearer " + token);
    }

    @Test
    void sinTokenEsUn401() {
        assertThrows(NoAutorizadoException.class, () -> sesion.usuario());
        assertNull(sesion.usuarioOpcional());
    }

    @Test
    void unTokenInvalidoEsUn401() {
        when(peticion.getHeader("Authorization")).thenReturn("Bearer inventado");
        assertThrows(NoAutorizadoException.class, () -> sesion.exigirEmpleado());
    }

    @Test
    void elHuespedNoEntraAFuncionesDeRecepcionNiDeAdministracion() {
        conSesionDe("laura", "laura123");
        assertThrows(AccesoDenegadoException.class, () -> sesion.exigirEmpleado());
        assertThrows(AccesoDenegadoException.class, () -> sesion.exigirRol(Rol.ADMINISTRADOR));
        assertEquals(Rol.HUESPED, sesion.exigirRol(Rol.HUESPED).getRol());
    }

    @Test
    void laRecepcionistaNoEntraAFuncionesDelAdministrador() {
        conSesionDe("recepcion", "recepcion123");
        assertDoesNotThrow(() -> sesion.exigirEmpleado());
        assertThrows(AccesoDenegadoException.class, () -> sesion.exigirRol(Rol.ADMINISTRADOR));
    }

    @Test
    void elAdministradorEntraATodo() {
        conSesionDe("admin", "admin123");
        assertDoesNotThrow(() -> sesion.exigirEmpleado());
        assertDoesNotThrow(() -> sesion.exigirRol(Rol.ADMINISTRADOR));
    }

    @Test
    void elHuespedSoloAccedeASusPropiasReservas() {
        Reserva deLaura = alojamiento.buscarReserva("R-001");
        Reserva deCarlos = alojamiento.buscarReserva("R-002");
        conSesionDe("laura", "laura123");
        assertDoesNotThrow(() -> sesion.exigirAccesoA(deLaura));
        assertThrows(AccesoDenegadoException.class, () -> sesion.exigirAccesoA(deCarlos));
    }

    @Test
    void recepcionAccedeACualquierReserva() {
        conSesionDe("recepcion", "recepcion123");
        assertDoesNotThrow(() -> sesion.exigirAccesoA(alojamiento.buscarReserva("R-002")));
    }

    @Test
    void unHuespedNuevoTampocoVeReservasAjenas() {
        alojamiento.registrarHuesped(new Huesped("Ana", LocalDate.of(2000, 1, 1), "555", "300", "a@x.co", "ana",
                "ana123"));
        Reserva propia = new ReservaService(mockCon(alojamiento)).crearDirecta("555", "APT-201",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(31),
                List.of(new Ocupante("Ana", LocalDate.of(2000, 1, 1))));
        conSesionDe("ana", "ana123");
        assertDoesNotThrow(() -> sesion.exigirAccesoA(propia));
        assertThrows(AccesoDenegadoException.class, () -> sesion.exigirAccesoA(alojamiento.buscarReserva("R-001")));
    }

    private AlojamientoRepositoryPort mockCon(Alojamiento a) {
        AlojamientoRepositoryPort repositorio = mock(AlojamientoRepositoryPort.class);
        when(repositorio.obtener()).thenReturn(a);
        return repositorio;
    }
}
