package notifications.notifications.infrastructure.adapter.out;

import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.infrastructure.adapter.out.persistence.DatosSemilla;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Comprueba que los datos semilla se cargan sin romper ninguna regla del dominio. */
class DatosSemillaTest {

    private final Alojamiento alojamiento = DatosSemilla.crear();

    @Test
    void cargaPersonasApartamentosYTemporadas() {
        assertEquals(3, alojamiento.getEmpleados().size());
        assertEquals(3, alojamiento.getHuespedes().size());
        assertEquals(4, alojamiento.getApartamentos().size());
        assertEquals(4, alojamiento.getPeriodosTemporada().size());
    }

    @Test
    void losUsuariosDeEjemploPuedenIniciarSesion() {
        assertEquals(Rol.ADMINISTRADOR, alojamiento.iniciarSesion("admin", "admin123").getRol());
        assertEquals(Rol.RECEPCIONISTA, alojamiento.iniciarSesion("recepcion", "recepcion123").getRol());
        assertEquals(Rol.HUESPED, alojamiento.iniciarSesion("laura", "laura123").getRol());
    }

    @Test
    void cargaUnaReservaDeCadaSituacion() {
        assertEquals(4, alojamiento.getReservas().size());
        assertEquals(EstadoReserva.CONFIRMADA, alojamiento.buscarReserva("R-001").getEstado());
        assertEquals(EstadoReserva.EN_CURSO, alojamiento.buscarReserva("R-002").getEstado());
        assertEquals(EstadoReserva.PENDIENTE, alojamiento.buscarReserva("R-003").getEstado());
        assertEquals(EstadoReserva.CONFIRMADA, alojamiento.buscarReserva("R-004").getEstado());
        assertEquals(1, alojamiento.getConflictosCanal().size());
    }

    @Test
    void losApartamentosQuedanConSuEstadoOperativo() {
        assertEquals(EstadoApartamento.PREPARADO, alojamiento.buscarApartamento("APT-101").getEstado());
        assertEquals(EstadoApartamento.OCUPADO, alojamiento.buscarApartamento("APT-102").getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, alojamiento.buscarApartamento("APT-201").getEstado());
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, alojamiento.buscarApartamento("APT-202").getEstado());
    }
}
