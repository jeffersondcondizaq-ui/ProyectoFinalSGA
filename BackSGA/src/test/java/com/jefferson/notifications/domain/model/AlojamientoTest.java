package com.jefferson.notifications.domain.model;

import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.DetalleNoche;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.reserva.ReservaPortal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoTest {

    private Alojamiento alojamiento;
    private Apartamento apartamento;
    private Huesped laura;
    private final LocalDate hoy = LocalDate.now();

    @BeforeEach
    void preparar() {
        alojamiento = new Alojamiento("Aparta-Suites UQ", "Salento");
        // Capacidad 2 para probar el límite fácilmente
        apartamento = new Apartamento("APT-101", "Suite Cocora", 1, 2, new ArrayList<>(),
                EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        alojamiento.registrarApartamento(apartamento);
        laura = new Huesped("Laura Gómez", LocalDate.of(1995, 7, 18), "1094123456",
                "3201234567", "laura@correo.com", "laura", "secreta");
        alojamiento.registrarHuesped(laura);
    }

    /** Crea una lista con la cantidad de ocupantes indicada. */
    private ArrayList<Ocupante> ocupantes(int cantidad) {
        ArrayList<Ocupante> lista = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            lista.add(new Ocupante("Ocupante " + i, LocalDate.of(1990, 1, 1)));
        }
        return lista;
    }

    private ReservaDirecta reserva(LocalDate entrada, LocalDate salida, int personas) {
        return new ReservaDirecta("R-1", laura, apartamento, entrada, salida, ocupantes(personas), null, null);
    }

    // ---------------- Regla: capacidad y fechas válidas ----------------

    @Test
    void creaLaReservaSiLasFechasYLaCapacidadSonValidas() {
        assertTrue(alojamiento.crearReserva(reserva(hoy, hoy.plusDays(2), 2)));
        assertEquals(1, alojamiento.getReservas().size());
    }

    @Test
    void rechazaSalidaIgualALaEntrada() {
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva(hoy.plusDays(3), hoy.plusDays(3), 1)));
    }

    @Test
    void rechazaSalidaAntesDeLaEntrada() {
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva(hoy.plusDays(5), hoy.plusDays(3), 1)));
    }

    @Test
    void rechazaEntradaAnteriorAHoy() {
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva(hoy.minusDays(1), hoy.plusDays(2), 1)));
    }

    @Test
    void rechazaMasOcupantesQueLaCapacidad() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva(hoy.plusDays(1), hoy.plusDays(3), 3)));
        assertEquals("El apartamento APT-101 admite máximo 2 personas", error.getMessage());
    }

    @Test
    void losMenoresTambienCuentanParaLaCapacidad() {
        ArrayList<Ocupante> grupo = ocupantes(2);
        grupo.add(new Ocupante("Bebé", hoy.minusYears(1))); // no paga, pero ocupa cupo
        ReservaDirecta reserva = new ReservaDirecta("R-1", laura, apartamento,
                hoy.plusDays(1), hoy.plusDays(3), grupo, null, null);
        assertThrows(ReglaNegocioException.class, () -> alojamiento.crearReserva(reserva));
    }

    @Test
    void rechazaUnaReservaSinOcupantes() {
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva(hoy.plusDays(1), hoy.plusDays(3), 0)));
        assertEquals(0, alojamiento.getReservas().size());
    }
    // ---------------- Regla: sin doble venta ----------------

    private ReservaDirecta reserva(String codigo, LocalDate entrada, LocalDate salida) {
        return new ReservaDirecta(codigo, laura, apartamento, entrada, salida, ocupantes(1), null, null);
    }

    @Test
    void rechazaUnaReservaQueSeCruzaConOtraActiva() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(4)));
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> alojamiento.crearReserva(reserva("R-2", hoy.plusDays(3), hoy.plusDays(6))));
        assertEquals("El apartamento APT-101 no está disponible en esas fechas", error.getMessage());
        assertEquals(1, alojamiento.getReservas().size());
    }

    @Test
    void permiteEntrarElMismoDiaQueSaleElAnterior() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        // La noche de salida no se cuenta: el día 3 queda libre para otra reserva
        assertTrue(alojamiento.crearReserva(reserva("R-2", hoy.plusDays(3), hoy.plusDays(5))));
    }

    @Test
    void unaReservaCanceladaLiberaLasFechas() {
        ReservaDirecta primera = reserva("R-1", hoy.plusDays(1), hoy.plusDays(3));
        alojamiento.crearReserva(primera);
        primera.cambiarEstado(EstadoReserva.CANCELADA);
        assertTrue(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(3)));
    }

    @Test
    void lasReservasDeOtroCanalTambienOcupanElApartamento() {
        ReservaExterna deBooking = new ReservaExterna("R-1", laura, apartamento, hoy.plusDays(1),
                hoy.plusDays(3), ocupantes(1), null, null, "BK-555", "Booking");
        alojamiento.crearReserva(deBooking);
        assertFalse(alojamiento.estaDisponible(apartamento, hoy.plusDays(2), hoy.plusDays(4)));
    }

    @Test
    void noEstaDisponibleSiAlgunaNocheEstaBloqueada() {
        alojamiento.registrarBloqueo("APT-101", new Bloqueo(hoy.plusDays(5), hoy.plusDays(6), "Pintura"));
        assertFalse(alojamiento.estaDisponible(apartamento, hoy.plusDays(3), hoy.plusDays(7)));
        assertTrue(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(5)));
    }

    @Test
    void noEstaDisponibleSiEstaFueraDeServicio() {
        apartamento.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Daño en la tubería");
        assertFalse(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(3)));
    }

    @Test
    void consultarDisponiblesFiltraPorFechasYPersonas() {
        Apartamento grande = new Apartamento("APT-201", "Suite Valle", 2, 4, new ArrayList<>(),
                EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        alojamiento.registrarApartamento(grande);
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));

        // APT-101 está ocupado esas noches: solo queda APT-201
        ArrayList<Apartamento> libres = alojamiento.consultarDisponibles(hoy.plusDays(2), hoy.plusDays(4), 2);
        assertEquals(1, libres.size());
        assertEquals("APT-201", libres.get(0).getCodigo());

        // Para 3 personas APT-101 no sirve aunque esté libre
        ArrayList<Apartamento> paraTres = alojamiento.consultarDisponibles(hoy.plusDays(10), hoy.plusDays(12), 3);
        assertEquals(1, paraTres.size());
        assertEquals("APT-201", paraTres.get(0).getCodigo());
    }

    @Test
    void consultarDisponiblesValidaLasFechas() {
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.consultarDisponibles(hoy.plusDays(4), hoy.plusDays(2), 2));
    }

    // ---------------- Regla: cálculo del valor noche por noche ----------------

    /** Tarifas por persona y noche: Baja 100.000, Media 150.000, Alta 200.000. */
    private void definirTarifas() {
        apartamento.definirTarifa(TipoTemporada.BAJA, 100_000);
        apartamento.definirTarifa(TipoTemporada.MEDIA, 150_000);
        apartamento.definirTarifa(TipoTemporada.ALTA, 200_000);
    }

    private long total(ArrayList<DetalleNoche> detalle) {
        long suma = 0;
        for (DetalleNoche noche : detalle) {
            suma += noche.calcularSubtotal();
        }
        return suma;
    }

    @Test
    void cotizaUnaNochePorCadaNocheDeLaEstancia() {
        definirTarifas();
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(4), ocupantes(2));
        assertEquals(3, detalle.size());
        assertEquals(hoy.plusDays(1), detalle.get(0).getFecha());
        assertEquals(hoy.plusDays(3), detalle.get(2).getFecha());
        // 3 noches en Baja x 2 personas x 100.000
        assertEquals(600_000, total(detalle));
    }

    @Test
    void unaEstanciaPuedeCruzarTemporadas() {
        definirTarifas();
        // Las noches 3 y 4 son temporada Alta
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.ALTA,
                hoy.plusDays(3), hoy.plusDays(5), "Puente festivo"));
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(6), ocupantes(1));

        assertEquals(TipoTemporada.BAJA, detalle.get(1).getTemporada()); // noche 2
        assertEquals(TipoTemporada.ALTA, detalle.get(2).getTemporada()); // noche 3
        assertEquals(TipoTemporada.ALTA, detalle.get(3).getTemporada()); // noche 4
        assertEquals(TipoTemporada.BAJA, detalle.get(4).getTemporada()); // noche 5
        // 3 noches Baja (300.000) + 2 noches Alta (400.000)
        assertEquals(700_000, total(detalle));
    }

    @Test
    void losMenoresDeSeisAniosNoPagan() {
        definirTarifas();
        ArrayList<Ocupante> grupo = ocupantes(1);
        grupo.add(new Ocupante("Niña de 3", hoy.minusYears(3)));
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(3), grupo);
        assertEquals(1, detalle.get(0).getPagadores());
        assertEquals(200_000, total(detalle));
    }

    @Test
    void laEdadSeCalculaALaFechaDeEntrada() {
        definirTarifas();
        LocalDate entrada = hoy.plusDays(10);
        ArrayList<Ocupante> grupo = new ArrayList<>();
        grupo.add(new Ocupante("Cumple 6 justo al llegar", entrada.minusYears(6)));
        grupo.add(new Ocupante("Cumple 6 al día siguiente", entrada.minusYears(6).plusDays(1)));
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", entrada, entrada.plusDays(1), grupo);
        assertEquals(1, detalle.get(0).getPagadores());
    }

    @Test
    void laReservaGuardaElValorDeLaCotizacion() {
        definirTarifas();
        ArrayList<Ocupante> grupo = ocupantes(2);
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(3), grupo);
        ReservaDirecta nueva = new ReservaDirecta("R-1", laura, apartamento, hoy.plusDays(1), hoy.plusDays(3),
                grupo, detalle, alojamiento.getPoliticaCancelacion());
        alojamiento.crearReserva(nueva);
        assertEquals(400_000, nueva.getValorEstancia());
    }

    @Test
    void noCotizaSiFaltaLaTarifaDeLaTemporada() {
        apartamento.definirTarifa(TipoTemporada.BAJA, 100_000); // sin tarifa Alta
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.ALTA,
                hoy.plusDays(2), hoy.plusDays(3), "Puente festivo"));
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(3), ocupantes(1)));
        assertEquals("El apartamento APT-101 no tiene tarifa definida para temporada ALTA", error.getMessage());
    }

    @Test
    void cotizarAplicaLasReglasDeFechasYCapacidad() {
        definirTarifas();
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.cotizar("APT-101", hoy.plusDays(3), hoy.plusDays(1), ocupantes(1)));
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(3), ocupantes(3)));
    }
    // ---------------- Regla: ciclo de vida y vencimiento a 24 horas ----------------

    private final LocalDateTime ahora = LocalDateTime.now();

    @Test
    void confirmaUnaReservaDirectaPendiente() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        assertTrue(alojamiento.confirmarReserva("R-1"));
        assertEquals(EstadoReserva.CONFIRMADA, alojamiento.buscarReserva("R-1").getEstado());
    }

    @Test
    void noConfirmaDosVeces() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        alojamiento.confirmarReserva("R-1");
        assertThrows(ReglaNegocioException.class, () -> alojamiento.confirmarReserva("R-1"));
    }

    @Test
    void noConfirmaUnaReservaCancelada() {
        ReservaDirecta cancelada = reserva("R-1", hoy.plusDays(1), hoy.plusDays(3));
        alojamiento.crearReserva(cancelada);
        cancelada.cambiarEstado(EstadoReserva.CANCELADA);
        assertThrows(ReglaNegocioException.class, () -> alojamiento.confirmarReserva("R-1"));
        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
    }

    @Test
    void laReservaPorPortalNecesitaElAnticipoParaConfirmarse() {
        definirTarifas();
        ArrayList<Ocupante> grupo = ocupantes(1);
        ArrayList<DetalleNoche> detalle = alojamiento.cotizar("APT-101", hoy.plusDays(1), hoy.plusDays(3), grupo);
        ReservaPortal web = new ReservaPortal("R-1", laura, apartamento, hoy.plusDays(1), hoy.plusDays(3),
                grupo, detalle, alojamiento.getPoliticaCancelacion());
        alojamiento.crearReserva(web);

        // Valor 200.000: anticipo 60.000. Sin pagar no se confirma
        assertThrows(ReglaNegocioException.class, () -> alojamiento.confirmarReserva("R-1"));

        web.getFolio().registrarPago(new Pago(MedioPago.TARJETA, 60_000, "Anticipo 30%", false));
        assertTrue(alojamiento.confirmarReserva("R-1"));
    }

    @Test
    void cancelaLasPendientesDeMasDe24Horas() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        assertEquals(1, alojamiento.cancelarPendientesVencidas(ahora.plusHours(25)));
        assertEquals(EstadoReserva.CANCELADA, alojamiento.buscarReserva("R-1").getEstado());
    }

    @Test
    void noCancelaAntesDeLas24Horas() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        assertEquals(0, alojamiento.cancelarPendientesVencidas(ahora.plusHours(23)));
        assertEquals(EstadoReserva.PENDIENTE, alojamiento.buscarReserva("R-1").getEstado());
    }

    @Test
    void lasConfirmadasNoVencen() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        alojamiento.confirmarReserva("R-1");
        assertEquals(0, alojamiento.cancelarPendientesVencidas(ahora.plusDays(3)));
        assertEquals(EstadoReserva.CONFIRMADA, alojamiento.buscarReserva("R-1").getEstado());
    }

    @Test
    void alVencerseLiberaLasNoches() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(3)));
        assertFalse(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(3)));
        alojamiento.cancelarPendientesVencidas(ahora.plusHours(25));
        assertTrue(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(3)));
    }
    // ---------------- Regla: check-in, check-out y preparación ----------------

    /** Crea y confirma una reserva que entra hoy y sale en 2 días. */
    private ReservaDirecta confirmadaParaHoy() {
        ReservaDirecta r = reserva("R-1", hoy, hoy.plusDays(2));
        alojamiento.crearReserva(r);
        alojamiento.confirmarReserva("R-1");
        return r;
    }

    @Test
    void elCheckInPoneLaReservaEnCursoYElApartamentoOcupado() {
        ReservaDirecta r = confirmadaParaHoy();
        assertTrue(alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 30)));
        assertEquals(EstadoReserva.EN_CURSO, r.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, apartamento.getEstado());
    }

    @Test
    void noHayCheckInDeUnaReservaSinConfirmar() {
        alojamiento.crearReserva(reserva("R-1", hoy, hoy.plusDays(2)));
        assertThrows(ReglaNegocioException.class, () -> alojamiento.hacerCheckIn("R-1", hoy.atTime(16, 0)));
    }

    @Test
    void noHayCheckInAntesDeLaFechaYHoraDeEntrada() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(2), hoy.plusDays(4)));
        alojamiento.confirmarReserva("R-1");
        // Un día antes
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.hacerCheckIn("R-1", hoy.plusDays(1).atTime(16, 0)));
        // El mismo día, pero antes de las 3:00 p. m.
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.hacerCheckIn("R-1", hoy.plusDays(2).atTime(14, 59)));
        assertTrue(alojamiento.hacerCheckIn("R-1", hoy.plusDays(2).atTime(15, 0)));
    }

    @Test
    void noHayCheckInSiElApartamentoNoEstaPreparado() {
        confirmadaParaHoy();
        apartamento.cambiarEstado(EstadoApartamento.EN_PREPARACION, null);
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 0)));
        assertEquals("El apartamento APT-101 no está Preparado (estado actual: EN_PREPARACION)", error.getMessage());
    }

    @Test
    void elCheckOutFinalizaLaReservaYDejaElApartamentoPendienteDePreparacion() {
        ReservaDirecta r = confirmadaParaHoy();
        alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 0));
        assertTrue(alojamiento.hacerCheckOut("R-1"));
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apartamento.getEstado());
    }

    @Test
    void noHayCheckOutSinCheckIn() {
        confirmadaParaHoy();
        assertThrows(ReglaNegocioException.class, () -> alojamiento.hacerCheckOut("R-1"));
    }

    @Test
    void despuesDelCheckOutHayQuePrepararAntesDelSiguienteCheckIn() {
        // Primer grupo: hoy -> mañana
        alojamiento.crearReserva(reserva("R-1", hoy, hoy.plusDays(1)));
        alojamiento.confirmarReserva("R-1");
        alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 0));
        // Segundo grupo: entra mañana, el mismo día que sale el primero
        alojamiento.crearReserva(reserva("R-2", hoy.plusDays(1), hoy.plusDays(3)));
        alojamiento.confirmarReserva("R-2");

        alojamiento.hacerCheckOut("R-1");
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.hacerCheckIn("R-2", hoy.plusDays(1).atTime(15, 0)));

        apartamento.cambiarEstado(EstadoApartamento.EN_PREPARACION, null);
        apartamento.cambiarEstado(EstadoApartamento.PREPARADO, null);
        assertTrue(alojamiento.hacerCheckIn("R-2", hoy.plusDays(1).atTime(15, 0)));
    }

    @Test
    void declaraNoShowDespuesDeLas10DeLaNoche() {
        ReservaDirecta r = confirmadaParaHoy();
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.declararNoShow("R-1", hoy.atTime(21, 59)));
        assertTrue(alojamiento.declararNoShow("R-1", hoy.atTime(22, 1)));
        assertEquals(EstadoReserva.NO_SHOW, r.getEstado());
        // El no-show libera las noches
        assertTrue(alojamiento.estaDisponible(apartamento, hoy.plusDays(1), hoy.plusDays(2)));
    }

    @Test
    void noHayNoShowSiElGrupoYaLlego() {
        confirmadaParaHoy();
        alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 0));
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.declararNoShow("R-1", hoy.atTime(23, 0)));
    }
    // ---------------- Regla: política de cancelación y valor congelado ----------------

    /**
     * Reserva de 2 noches para 1 persona en Baja (valor 200.000) que entra en "diasAntes" días,
     * con el 30 % (60.000) ya pagado.
     */
    private ReservaDirecta reservaPagada(int diasAntes) {
        definirTarifas();
        LocalDate entrada = hoy.plusDays(diasAntes);
        ArrayList<Ocupante> grupo = ocupantes(1);
        ReservaDirecta r = new ReservaDirecta("R-1", laura, apartamento, entrada, entrada.plusDays(2), grupo,
                alojamiento.cotizar("APT-101", entrada, entrada.plusDays(2), grupo),
                alojamiento.getPoliticaCancelacion());
        alojamiento.crearReserva(r);
        r.getFolio().registrarPago(new Pago(MedioPago.TRANSFERENCIA, 60_000, "Anticipo", false));
        return r;
    }

    /** Momento a "horas" horas de la entrada (3:00 p. m. del día de entrada). */
    private LocalDateTime horasAntesDeEntrar(ReservaDirecta r, int horas) {
        return r.getFechaEntrada().atTime(15, 0).minusHours(horas);
    }

    @Test
    void conMasDe7DiasSeDevuelveTodoLoPagado() {
        ReservaDirecta r = reservaPagada(10);
        assertEquals(60_000, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 8 * 24)));
    }

    @Test
    void entre2y7DiasSeRetieneElCincuentaPorCientoDeLaEstancia() {
        ReservaDirecta r = reservaPagada(10);
        r.getFolio().registrarPago(new Pago(MedioPago.EFECTIVO, 140_000, "Resto", false)); // pagó todo
        // Retiene 50 % de 200.000 = 100.000; devuelve 200.000 - 100.000
        assertEquals(100_000, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 5 * 24)));
        // Justo en el límite de 48 horas todavía es 50 %
        assertEquals(100_000, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 48)));
    }

    @Test
    void conMenosDe48HorasSeRetieneTodo() {
        ReservaDirecta r = reservaPagada(10);
        assertEquals(0, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 47)));
    }

    @Test
    void laRetencionNuncaDevuelveMasDeLoPagado() {
        ReservaDirecta r = reservaPagada(10);
        // Pagó 60.000 y se retienen 100.000: no se devuelve nada (no queda negativo)
        assertEquals(0, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 5 * 24)));
    }

    @Test
    void cancelarLiberaLasNochesYDejaElFolioEnCero() {
        ReservaDirecta r = reservaPagada(10);
        long devuelto = alojamiento.cancelarReserva("R-1", horasAntesDeEntrar(r, 8 * 24));

        assertEquals(60_000, devuelto);
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(0, r.getFolio().calcularSaldo());
        assertTrue(alojamiento.estaDisponible(apartamento, r.getFechaEntrada(), r.getFechaSalida()));
    }

    @Test
    void cancelarConRetencionRegistraLaPenalidad() {
        ReservaDirecta r = reservaPagada(10);
        long devuelto = alojamiento.cancelarReserva("R-1", horasAntesDeEntrar(r, 24));
        assertEquals(0, devuelto);
        assertEquals(60_000, r.getFolio().calcularTotalCargos()); // solo queda la penalidad
        assertEquals(0, r.getFolio().calcularSaldo());
    }

    @Test
    void noSeCancelaUnaReservaEnCurso() {
        confirmadaParaHoy();
        alojamiento.hacerCheckIn("R-1", hoy.atTime(15, 0));
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.cancelarReserva("R-1", hoy.atTime(16, 0)));
    }

    @Test
    void enNoShowSeRetieneTodo() {
        ReservaDirecta r = reservaPagada(0);
        alojamiento.confirmarReserva("R-1");
        alojamiento.declararNoShow("R-1", hoy.atTime(23, 0));
        assertEquals(60_000, r.getFolio().calcularTotalCargos()); // lo pagado queda como penalidad
        assertEquals(0, r.getFolio().calcularSaldo());
    }

    @Test
    void elValorYLaPoliticaQuedanCongeladosAlCrearLaReserva() {
        ReservaDirecta r = reservaPagada(10);
        r.getFolio().registrarPago(new Pago(MedioPago.EFECTIVO, 140_000, "Resto", false));

        // Después de crear la reserva suben las tarifas y la política se vuelve más estricta
        apartamento.definirTarifa(TipoTemporada.BAJA, 999_000);
        alojamiento.configurarPolitica(new PoliticaCancelacion(30, 48, 100));

        assertEquals(200_000, r.getValorEstancia());
        // Con la política congelada (7 días, 50 %) a 8 días de entrar se devuelve todo
        assertEquals(200_000, alojamiento.calcularDevolucion("R-1", horasAntesDeEntrar(r, 8 * 24)));
    }

    @Test
    void elFolioArrancaConElCargoDelAlojamiento() {
        ReservaDirecta r = reservaPagada(10);
        assertEquals(200_000, r.getFolio().calcularTotalCargos());
        assertEquals(140_000, r.getFolio().calcularSaldo()); // 200.000 - 60.000 pagados
    }
    // ---------------- Regla: reservas externas y conflictos de canal ----------------

    private ReservaExterna externa(String codigo, String codigoExterno, LocalDate entrada, LocalDate salida) {
        definirTarifas();
        ArrayList<Ocupante> grupo = ocupantes(2);
        return new ReservaExterna(codigo, laura, apartamento, entrada, salida, grupo,
                alojamiento.cotizar("APT-101", entrada, salida, grupo),
                alojamiento.getPoliticaCancelacion(), codigoExterno, "Booking");
    }

    @Test
    void recibeUnaReservaExternaSinChoqueYQuedaConfirmada() {
        ReservaExterna r = externa("R-1", "BK-100", hoy.plusDays(1), hoy.plusDays(3));
        assertTrue(alojamiento.recibirReservaExterna(r));
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals(1, alojamiento.getReservas().size());
    }

    @Test
    void laComisionDeLaPlataformaEsDel15PorCiento() {
        // 2 noches x 2 personas x 100.000 = 400.000; comisión 60.000
        ReservaExterna r = externa("R-1", "BK-100", hoy.plusDays(1), hoy.plusDays(3));
        alojamiento.recibirReservaExterna(r);
        assertEquals(400_000, r.getValorEstancia());
        assertEquals(60_000, r.calcularComision());
    }

    @Test
    void unaExternaQueChocaSeRechazaYQuedaComoConflicto() {
        ReservaDirecta directa = reserva("R-1", hoy.plusDays(1), hoy.plusDays(4));
        alojamiento.crearReserva(directa);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> alojamiento.recibirReservaExterna(externa("R-2", "BK-200", hoy.plusDays(3), hoy.plusDays(5))));
        assertEquals("La reserva externa BK-200 choca con la reserva R-1 y se registró como conflicto de canal",
                error.getMessage());

        assertEquals(1, alojamiento.getConflictosCanal().size());
        assertEquals("BK-200", alojamiento.getConflictosCanal().get(0).getCodigoExterno());
        assertEquals(directa, alojamiento.getConflictosCanal().get(0).getReservaExistente());
    }

    @Test
    void elConflictoNuncaReemplazaALaReservaExistente() {
        ReservaDirecta directa = reserva("R-1", hoy.plusDays(1), hoy.plusDays(4));
        alojamiento.crearReserva(directa);
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.recibirReservaExterna(externa("R-2", "BK-200", hoy.plusDays(1), hoy.plusDays(4))));

        assertEquals(1, alojamiento.getReservas().size());
        assertEquals(directa, alojamiento.getReservas().get(0));
        assertEquals(EstadoReserva.PENDIENTE, directa.getEstado());
    }

    @Test
    void unMismoCodigoExternoNoCreaDosReservas() {
        alojamiento.recibirReservaExterna(externa("R-1", "BK-100", hoy.plusDays(1), hoy.plusDays(3)));
        assertThrows(ReglaNegocioException.class,
                () -> alojamiento.recibirReservaExterna(externa("R-2", "BK-100", hoy.plusDays(10), hoy.plusDays(12))));
        assertEquals(1, alojamiento.getReservas().size());
    }

    @Test
    void unaExternaNoChocaConUnaReservaCancelada() {
        ReservaDirecta cancelada = reserva("R-1", hoy.plusDays(1), hoy.plusDays(3));
        alojamiento.crearReserva(cancelada);
        cancelada.cambiarEstado(EstadoReserva.CANCELADA);

        assertTrue(alojamiento.recibirReservaExterna(externa("R-2", "BK-300", hoy.plusDays(1), hoy.plusDays(3))));
        assertEquals(0, alojamiento.getConflictosCanal().size());
    }

    @Test
    void lasExternasCumplenLasDemasReglas() {
        // Capacidad: el apartamento admite 2, la externa trae 3
        ArrayList<Ocupante> grupo = ocupantes(3);
        ReservaExterna muchos = new ReservaExterna("R-1", laura, apartamento, hoy.plusDays(1), hoy.plusDays(3),
                grupo, null, null, "BK-400", "Booking");
        assertThrows(ReglaNegocioException.class, () -> alojamiento.recibirReservaExterna(muchos));
        assertEquals(0, alojamiento.getReservas().size());
    }

    // ---------------- Reportes ----------------

    @Test
    void laOcupacionCuentaLasNochesVendidasEnElRango() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(4)));
        alojamiento.confirmarReserva("R-1");
        alojamiento.crearReserva(reserva("R-2", hoy.plusDays(5), hoy.plusDays(6))); // Pendiente: no cuenta
        // Rango de hoy+2 a hoy+10: de R-1 entran las noches 2 y 3
        assertEquals(2, alojamiento.reporteOcupacion(hoy.plusDays(2), hoy.plusDays(10)).get(apartamento).intValue());
    }

    @Test
    void losIngresosSonPagosMenosDevoluciones() {
        ReservaDirecta r = reservaPagada(10); // pagó 60.000 hoy
        assertEquals(60_000, alojamiento.reporteIngresos(hoy, hoy));
        alojamiento.cancelarReserva("R-1", horasAntesDeEntrar(r, 8 * 24)); // devuelve 60.000
        assertEquals(0, alojamiento.reporteIngresos(hoy, hoy));
    }

    @Test
    void cuentaLasReservasPorCanal() {
        alojamiento.crearReserva(reserva("R-1", hoy.plusDays(1), hoy.plusDays(2)));
        alojamiento.recibirReservaExterna(externa("R-2", "BK-1", hoy.plusDays(3), hoy.plusDays(4)));
        assertEquals(1, alojamiento.reporteReservasPorCanal().get(Canal.DIRECTA).intValue());
        assertEquals(1, alojamiento.reporteReservasPorCanal().get(Canal.EXTERNA).intValue());
        assertEquals(0, alojamiento.reporteReservasPorCanal().get(Canal.PORTAL).intValue());
    }

    @Test
    void losSaldosPendientesSonLasReservasQueDebenDinero() {
        reservaPagada(10); // valor 200.000, pagó 60.000
        assertEquals(1, alojamiento.reporteSaldosPendientes().size());
    }
}
