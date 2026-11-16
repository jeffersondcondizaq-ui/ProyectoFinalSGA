package com.jefferson.notifications.domain.model;

import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
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
}