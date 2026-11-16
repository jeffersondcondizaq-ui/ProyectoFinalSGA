package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reserva abstracta, sus 3 canales, DetalleNoche y ConflictoCanal. */
class ReservaTest {

    private final LocalDate entrada = LocalDate.of(2026, 12, 10);
    private final Huesped laura = new Huesped("Laura", LocalDate.of(1995, 7, 18), "1094", "320", "l@x.co",
            "laura", "secreta");
    private final Apartamento apto = new Apartamento("APT-101", "Suite", 2, 4, new ArrayList<>(),
            EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
            new ArrayList<>());

    /** 2 noches a 100.000 por persona con 2 pagadores: 400.000. */
    private ArrayList<DetalleNoche> detalle() {
        return new ArrayList<>(List.of(
                new DetalleNoche(entrada, TipoTemporada.BAJA, 100_000, 2),
                new DetalleNoche(entrada.plusDays(1), TipoTemporada.BAJA, 100_000, 2)));
    }

    private ArrayList<Ocupante> familia() {
        return new ArrayList<>(List.of(
                new Ocupante("Laura", LocalDate.of(1995, 7, 18)),
                new Ocupante("Andrés", LocalDate.of(1993, 2, 5)),
                new Ocupante("Sofía", LocalDate.of(2022, 3, 10))));
    }

    private ReservaDirecta directa() {
        return new ReservaDirecta("R-1", laura, apto, entrada, entrada.plusDays(2), familia(), detalle(), null);
    }

    @Test
    void elValorEsLaSumaDelDetalleNochePorNoche() {
        assertEquals(400_000, directa().getValorEstancia());
        assertEquals(200_000, new DetalleNoche(entrada, TipoTemporada.ALTA, 100_000, 2).calcularSubtotal());
    }

    @Test
    void cuentaNochesYPagadores() {
        ReservaDirecta r = directa();
        assertEquals(2, r.calcularNoches());
        assertEquals(2, r.contarPagadores()); // Sofía tiene 4 años: no paga
    }

    @Test
    void contieneSusNochesPeroNoLaDeSalida() {
        ReservaDirecta r = directa();
        assertTrue(r.contieneNoche(entrada));
        assertTrue(r.contieneNoche(entrada.plusDays(1)));
        assertFalse(r.contieneNoche(entrada.plusDays(2)));
    }

    @Test
    void detectaCrucesDeFechas() {
        ReservaDirecta r = directa();
        assertTrue(r.seCruzaCon(entrada.plusDays(1), entrada.plusDays(4)));
        assertFalse(r.seCruzaCon(entrada.plusDays(2), entrada.plusDays(4))); // entra el día que sale la otra
    }

    @Test
    void soloCambiaDeEstadoSegunLaTabla() {
        ReservaDirecta r = directa();
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertFalse(r.cambiarEstado(EstadoReserva.EN_CURSO));
        assertTrue(r.cambiarEstado(EstadoReserva.CONFIRMADA));
        assertTrue(r.estaActiva());
        assertTrue(r.cambiarEstado(EstadoReserva.CANCELADA));
        assertFalse(r.estaActiva());
    }

    @Test
    void venceAlPasar24HorasPendiente() {
        ReservaDirecta r = directa();
        assertFalse(r.estaVencida(LocalDateTime.now().plusHours(23)));
        assertTrue(r.estaVencida(LocalDateTime.now().plusHours(25)));
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        assertFalse(r.estaVencida(LocalDateTime.now().plusHours(25)));
    }

    @Test
    void congelaUnaCopiaDeLaPolitica() {
        PoliticaCancelacion vigente = new PoliticaCancelacion();
        ReservaDirecta r = new ReservaDirecta("R-1", laura, apto, entrada, entrada.plusDays(2), familia(), detalle(),
                vigente);
        vigente.setPorcentajeRetencionParcial(90);
        assertNotSame(vigente, r.getPolitica());
        assertEquals(50, r.getPolitica().getPorcentajeRetencionParcial());
    }

    @Test
    void laDirectaSeConfirmaSinAnticipo() {
        assertEquals(Canal.DIRECTA, directa().getCanal());
        assertTrue(directa().puedeConfirmarse());
        assertEquals(0, directa().calcularComision());
    }

    @Test
    void laDelPortalExigeElAnticipoDel30PorCiento() {
        ReservaPortal web = new ReservaPortal("R-2", laura, apto, entrada, entrada.plusDays(2), familia(), detalle(),
                null);
        assertEquals(Canal.PORTAL, web.getCanal());
        assertEquals(120_000, web.calcularAnticipo());
        assertFalse(web.puedeConfirmarse());
        web.getFolio().registrarPago(new Pago(MedioPago.TARJETA, 120_000, "Anticipo", false));
        assertTrue(web.puedeConfirmarse());
    }

    @Test
    void laExternaPagaComisionDel15PorCiento() {
        ReservaExterna booking = new ReservaExterna("R-3", laura, apto, entrada, entrada.plusDays(2), familia(),
                detalle(), null, "BK-1", "Booking");
        assertEquals(Canal.EXTERNA, booking.getCanal());
        assertEquals(60_000, booking.calcularComision());
        assertEquals("BK-1", booking.getCodigoExterno());
    }

    @Test
    void elConflictoGuardaConCualReservaChoco() {
        ReservaDirecta existente = directa();
        ConflictoCanal conflicto = new ConflictoCanal("BK-9", LocalDateTime.now(), "Choque", existente);
        assertEquals("BK-9", conflicto.getCodigoExterno());
        assertEquals(existente, conflicto.getReservaExistente());
    }
}
