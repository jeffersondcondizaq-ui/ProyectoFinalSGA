package com.jefferson.notifications.domain.model.folio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Folio y sus movimientos: Cargo, Pago y Devolucion. Saldo = cargos − pagos. */
class FolioTest {

    private Folio folio;
    private Cargo alojamiento;

    @BeforeEach
    void preparar() {
        folio = new Folio();
        alojamiento = new Cargo(TipoCargo.ALOJAMIENTO, 500_000, "Alojamiento 2 noches", false);
        folio.agregarCargo(alojamiento);
    }

    @Test
    void elSaldoEsCargosMenosPagos() {
        folio.registrarPago(new Pago(MedioPago.EFECTIVO, 150_000, "Abono", false));
        assertEquals(500_000, folio.calcularTotalCargos());
        assertEquals(150_000, folio.calcularTotalPagos());
        assertEquals(350_000, folio.calcularSaldo());
    }

    @Test
    void admiteVariosPagosParcialesConDistintosMedios() {
        folio.registrarPago(new Pago(MedioPago.TARJETA, 200_000, "Anticipo", false));
        folio.registrarPago(new Pago(MedioPago.TRANSFERENCIA, 300_000, "Resto", false));
        assertEquals(0, folio.calcularSaldo());
        assertEquals(3, folio.getMovimientos().size());
    }

    @Test
    void rechazaMovimientosSinValor() {
        assertFalse(folio.registrarPago(new Pago(MedioPago.EFECTIVO, 0, "Nada", false)));
        assertFalse(folio.agregarCargo(null));
    }

    @Test
    void unaCorreccionEsUnMovimientoContrarioYNoBorraNada() {
        Pago errado = new Pago(MedioPago.EFECTIVO, 100_000, "Pago mal digitado", false);
        folio.registrarPago(errado);
        assertTrue(folio.corregir(errado));
        assertEquals(3, folio.getMovimientos().size()); // el original sigue ahí
        assertEquals(500_000, folio.calcularSaldo());
    }

    @Test
    void unaCorreccionNoSePuedeCorregir() {
        folio.corregir(alojamiento);
        MovimientoFolio correccion = folio.getMovimientos().get(1);
        assertTrue(correccion.isEsCorreccion());
        assertFalse(folio.corregir(correccion));
    }

    @Test
    void soloSeCorrigenMovimientosDelMismoFolio() {
        assertFalse(folio.corregir(new Cargo(TipoCargo.PENALIDAD, 10_000, "Ajeno", false)));
    }

    @Test
    void unaDevolucionAumentaElSaldo() {
        folio.registrarPago(new Pago(MedioPago.TARJETA, 500_000, "Total", false));
        folio.registrarDevolucion(new Devolucion(100_000, "Devolución", false));
        assertEquals(100_000, folio.calcularSaldo());
        assertEquals(400_000, folio.calcularTotalPagos());
    }
}
