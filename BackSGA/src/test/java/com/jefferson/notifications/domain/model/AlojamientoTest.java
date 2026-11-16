package com.jefferson.notifications.domain.model;

import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}