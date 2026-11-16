package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.RecursoNoEncontradoException;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.folio.Folio;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.TipoCargo;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.jefferson.notifications.application.service.AlojamientoDePrueba.HOY;
import static com.jefferson.notifications.application.service.AlojamientoDePrueba.adultos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FolioServiceTest {

    private AlojamientoRepositoryPort repositorio;
    private FolioService folios;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        repositorio = mock(AlojamientoRepositoryPort.class);
        Alojamiento alojamiento = AlojamientoDePrueba.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
        folios = new FolioService(repositorio);
        reserva = new ReservaService(repositorio).crearDirecta("1094", "APT-101", HOY.plusDays(20), HOY.plusDays(22),
                adultos(2)); // valor 400.000
    }

    @Test
    void elFolioArrancaConElCargoDelAlojamiento() {
        assertEquals(400_000, folios.obtener(reserva.getCodigo()).calcularSaldo());
    }

    @Test
    void registraPagosParcialesYCargosAdicionales() {
        folios.registrarPago(reserva.getCodigo(), MedioPago.EFECTIVO, 100_000, "Abono");
        Folio folio = folios.registrarCargo(reserva.getCodigo(), TipoCargo.SERVICIO_ADICIONAL, 30_000, "Desayuno");
        assertEquals(330_000, folio.calcularSaldo());
    }

    @Test
    void elCargoDeAlojamientoNoSeRegistraAMano() {
        assertThrows(ReglaNegocioException.class,
                () -> folios.registrarCargo(reserva.getCodigo(), TipoCargo.ALOJAMIENTO, 10_000, "Extra"));
    }

    @Test
    void noAceptaPagosSinValor() {
        assertThrows(ReglaNegocioException.class,
                () -> folios.registrarPago(reserva.getCodigo(), MedioPago.EFECTIVO, 0, "Nada"));
    }

    @Test
    void corrigeConUnMovimientoContrario() {
        folios.registrarPago(reserva.getCodigo(), MedioPago.EFECTIVO, 100_000, "Mal digitado");
        Folio folio = folios.corregir(reserva.getCodigo(), 1);
        assertEquals(3, folio.getMovimientos().size());
        assertEquals(400_000, folio.calcularSaldo());
    }

    @Test
    void corregirUnMovimientoInexistenteEsUn404() {
        assertThrows(RecursoNoEncontradoException.class, () -> folios.corregir(reserva.getCodigo(), 9));
    }

    @Test
    void unaReservaCanceladaNoAdmiteMasMovimientos() {
        new ReservaService(repositorio).cancelar(reserva.getCodigo());
        assertThrows(ReglaNegocioException.class,
                () -> folios.registrarPago(reserva.getCodigo(), MedioPago.EFECTIVO, 10_000, "Tarde"));
    }
}
