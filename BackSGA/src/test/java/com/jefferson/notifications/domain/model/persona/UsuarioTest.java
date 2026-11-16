package com.jefferson.notifications.domain.model.persona;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Usuario, Huesped, Empleado, Recepcionista y Administrador. */
class UsuarioTest {

    private Huesped laura() {
        return new Huesped("Laura", LocalDate.of(1995, 7, 18), "1094", "320", "laura@correo.com", "laura", "secreta");
    }

    @Test
    void autenticaSoloConUsuarioYContrasenaCorrectos() {
        Huesped laura = laura();
        assertTrue(laura.autenticar("laura", "secreta"));
        assertFalse(laura.autenticar("laura", "otra"));
        assertFalse(laura.autenticar("otro", "secreta"));
    }

    @Test
    void cambiaLaContrasenaSoloSiLaActualEsCorrecta() {
        Huesped laura = laura();
        assertFalse(laura.cambiarContrasena("mala", "nueva123"));
        assertTrue(laura.cambiarContrasena("secreta", "nueva123"));
        assertTrue(laura.autenticar("laura", "nueva123"));
    }

    @Test
    void noAceptaUnaContrasenaNuevaVacia() {
        assertFalse(laura().cambiarContrasena("secreta", ""));
    }

    @Test
    void elHuespedActualizaTelefonoYCorreoValidos() {
        Huesped laura = laura();
        laura.actualizarPerfil("311", "nuevo@correo.com");
        assertEquals("311", laura.getTelefono());
        assertEquals("nuevo@correo.com", laura.getCorreo());
        laura.actualizarPerfil("312", "sin-arroba");
        assertEquals("311", laura.getTelefono()); // correo inválido: no cambia nada
    }

    @Test
    void cadaTipoDeUsuarioTieneSuRol() {
        LocalDate hoy = LocalDate.now();
        assertEquals(Rol.HUESPED, laura().getRol());
        assertEquals(Rol.RECEPCIONISTA, new Recepcionista("Julián", LocalDate.of(1998, 9, 30), "2", "3", "j@x.co",
                "recepcion", "123456", hoy).getRol());
        assertEquals(Rol.ADMINISTRADOR, new Administrador("Diana", LocalDate.of(1985, 4, 12), "1", "3", "d@x.co",
                "admin", "123456", hoy).getRol());
    }

    @Test
    void unEmpleadoDesvinculadoQuedaInactivo() {
        Empleado julian = new Recepcionista("Julián", LocalDate.of(1998, 9, 30), "2", "3", "j@x.co", "recepcion",
                "123456", LocalDate.now());
        assertTrue(julian.isActivo());
        julian.desvincular();
        assertFalse(julian.isActivo());
    }
}
