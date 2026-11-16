package notifications.notifications.infrastructure.adapter.out;

import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.infrastructure.adapter.out.persistence.DatosSemilla;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba que los datos semilla se cargan sin romper ninguna regla del dominio. */
class DatosSemillaTest {

    private final Alojamiento alojamiento = DatosSemilla.crear();

    @Test
    void cargaPersonasApartamentosYTemporadas() {
        assertEquals(5, alojamiento.getEmpleados().size());
        assertEquals(7, alojamiento.getHuespedes().size());
        assertEquals(8, alojamiento.getApartamentos().size());
        assertEquals(4, alojamiento.getPeriodosTemporada().size());
    }

    @Test
    void losUsuariosDeTodosLosRolesPuedenIniciarSesion() {
        assertEquals(Rol.ADMINISTRADOR, alojamiento.iniciarSesion("admin", "admin123").getRol());
        assertEquals(Rol.ADMINISTRADOR, alojamiento.iniciarSesion("gerencia", "gerencia123").getRol());
        assertEquals(Rol.RECEPCIONISTA, alojamiento.iniciarSesion("recepcion", "recepcion123").getRol());
        assertEquals(Rol.RECEPCIONISTA, alojamiento.iniciarSesion("ctorres", "camila123").getRol());
        assertEquals(Rol.HUESPED, alojamiento.iniciarSesion("laura", "laura123").getRol());
        assertEquals(Rol.HUESPED, alojamiento.iniciarSesion("sebastian", "sebastian123").getRol());
        assertThrows(RuntimeException.class, () -> alojamiento.iniciarSesion("pcardenas", "paula123"));
    }

    @Test
    void hayUnaReservaEnCadaEstado() {
        assertEquals(10, alojamiento.getReservas().size());
        assertEquals(EstadoReserva.CONFIRMADA, alojamiento.buscarReserva("R-001").getEstado());
        assertEquals(EstadoReserva.EN_CURSO, alojamiento.buscarReserva("R-002").getEstado());
        assertEquals(EstadoReserva.PENDIENTE, alojamiento.buscarReserva("R-003").getEstado());
        assertEquals(EstadoReserva.FINALIZADA, alojamiento.buscarReserva("R-005").getEstado());
        assertEquals(EstadoReserva.CANCELADA, alojamiento.buscarReserva("R-006").getEstado());
        assertEquals(EstadoReserva.NO_SHOW, alojamiento.buscarReserva("R-007").getEstado());
        assertEquals(1, alojamiento.getConflictosCanal().size());
    }

    @Test
    void hayReservasDeLosTresCanales() {
        assertEquals(4, alojamiento.reporteReservasPorCanal().get(Canal.DIRECTA).intValue());
        assertEquals(4, alojamiento.reporteReservasPorCanal().get(Canal.PORTAL).intValue());
        assertEquals(2, alojamiento.reporteReservasPorCanal().get(Canal.EXTERNA).intValue());
    }

    @Test
    void losApartamentosQuedanConSuEstadoOperativo() {
        assertEquals(EstadoApartamento.PREPARADO, alojamiento.buscarApartamento("APT-101").getEstado());
        assertEquals(EstadoApartamento.OCUPADO, alojamiento.buscarApartamento("APT-102").getEstado());
        assertEquals(EstadoApartamento.EN_PREPARACION, alojamiento.buscarApartamento("APT-103").getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, alojamiento.buscarApartamento("APT-201").getEstado());
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, alojamiento.buscarApartamento("APT-202").getEstado());
        assertFalse(alojamiento.buscarApartamento("APT-302").isActivo());
    }

    @Test
    void losApartamentosTienenFotosYHayNovedades() {
        assertEquals(3, alojamiento.buscarApartamento("APT-101").getFotos().size());
        assertEquals("APT-202", alojamiento.reporteApartamentosConMasNovedades().get(0).getCodigo());
        assertTrue(alojamiento.buscarHuesped("1094123456").getFoto().startsWith("https://"));
    }

    @Test
    void losFoliosDeEjemploCuadran() {
        assertEquals(0, alojamiento.buscarReserva("R-005").getFolio().calcularSaldo()); // pagada
        assertEquals(0, alojamiento.buscarReserva("R-006").getFolio().calcularSaldo()); // cancelada y devuelta
        assertEquals(0, alojamiento.buscarReserva("R-007").getFolio().calcularSaldo()); // no-show liquidado
        assertTrue(alojamiento.reporteSaldosPendientes().size() >= 3);
    }
}
