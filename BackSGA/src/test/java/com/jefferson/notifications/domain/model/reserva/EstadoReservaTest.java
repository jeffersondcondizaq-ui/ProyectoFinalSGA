package com.jefferson.notifications.domain.model.reserva;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tabla del ciclo de vida del enunciado. */
class EstadoReservaTest {

    @Test
    void transicionesPermitidas() {
        assertTrue(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.CONFIRMADA));
        assertTrue(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.CANCELADA));
        assertTrue(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.EN_CURSO));
        assertTrue(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.CANCELADA));
        assertTrue(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.NO_SHOW));
        assertTrue(EstadoReserva.EN_CURSO.puedePasarA(EstadoReserva.FINALIZADA));
    }

    @Test
    void transicionesQueNoEstanEnLaTabla() {
        assertFalse(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.EN_CURSO));
        assertFalse(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.NO_SHOW));
        assertFalse(EstadoReserva.EN_CURSO.puedePasarA(EstadoReserva.CANCELADA));
        assertFalse(EstadoReserva.CONFIRMADA.puedePasarA(EstadoReserva.FINALIZADA));
        assertFalse(EstadoReserva.PENDIENTE.puedePasarA(null));
    }

    @Test
    void losEstadosFinalesNoCambian() {
        for (EstadoReserva destino : EstadoReserva.values()) {
            assertFalse(EstadoReserva.FINALIZADA.puedePasarA(destino));
            assertFalse(EstadoReserva.CANCELADA.puedePasarA(destino));
            assertFalse(EstadoReserva.NO_SHOW.puedePasarA(destino));
        }
    }

    @Test
    void activaMientrasEstePendienteConfirmadaOEnCurso() {
        assertTrue(EstadoReserva.PENDIENTE.esActivo());
        assertTrue(EstadoReserva.CONFIRMADA.esActivo());
        assertTrue(EstadoReserva.EN_CURSO.esActivo());
        assertFalse(EstadoReserva.FINALIZADA.esActivo());
        assertFalse(EstadoReserva.CANCELADA.esActivo());
        assertFalse(EstadoReserva.NO_SHOW.esActivo());
    }
}
