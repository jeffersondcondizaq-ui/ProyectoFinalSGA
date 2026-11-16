package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.ReservaUseCase.ResultadoCancelacion;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.RecursoNoEncontradoException;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.jefferson.notifications.application.service.AlojamientoDePrueba.HOY;
import static com.jefferson.notifications.application.service.AlojamientoDePrueba.adultos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ReservaService con el repositorio simulado (Mockito): cada regla de negocio a través del caso de uso. */
class ReservaServiceTest {

    private AlojamientoRepositoryPort repositorio;
    private Alojamiento alojamiento;
    private ReservaService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(AlojamientoRepositoryPort.class);
        alojamiento = AlojamientoDePrueba.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
        servicio = new ReservaService(repositorio);
    }

    private Reserva directa(LocalDate entrada, LocalDate salida) {
        return servicio.crearDirecta("1094", "APT-101", entrada, salida, adultos(2));
    }

    @Test
    void creaLaReservaConCodigoYValorCalculadoPorElBack() {
        Reserva r = servicio.crearPortal("1094", "APT-101", HOY.plusDays(10), HOY.plusDays(12), adultos(2));
        assertEquals("R-001", r.getCodigo());
        assertEquals(400_000, r.getValorEstancia()); // 2 noches x 2 personas x 100.000
        verify(repositorio).guardar(alojamiento);
    }

    @Test
    void losCodigosSonConsecutivos() {
        directa(HOY.plusDays(1), HOY.plusDays(2));
        assertEquals("R-002", directa(HOY.plusDays(3), HOY.plusDays(4)).getCodigo());
    }

    @Test
    void siUnaReglaFallaNoSeGuardaNada() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crearDirecta("1094", "APT-101", HOY.plusDays(1), HOY.plusDays(3), adultos(3)));
        verify(repositorio, never()).guardar(alojamiento);
        assertEquals(0, alojamiento.getReservas().size());
    }

    @Test
    void noVendeDosVecesLaMismaNoche() {
        directa(HOY.plusDays(1), HOY.plusDays(4));
        assertThrows(ReglaNegocioException.class, () -> directa(HOY.plusDays(3), HOY.plusDays(5)));
    }

    @Test
    void elTitularDebeExistir() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.crearDirecta("999", "APT-101", HOY.plusDays(1), HOY.plusDays(2), adultos(1)));
    }

    @Test
    void laDisponibilidadExcluyeLosApartamentosOcupados() {
        directa(HOY.plusDays(1), HOY.plusDays(4));
        assertEquals(1, servicio.consultarDisponibles(HOY.plusDays(2), HOY.plusDays(3), 2).size());
    }

    @Test
    void cotizaNochePorNoche() {
        assertEquals(3, servicio.cotizar("APT-201", HOY.plusDays(1), HOY.plusDays(4), adultos(4)).size());
    }

    @Test
    void elPortalNoSeConfirmaSinAnticipo() {
        Reserva r = servicio.crearPortal("1094", "APT-101", HOY.plusDays(10), HOY.plusDays(12), adultos(2));
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(r.getCodigo()));
        r.getFolio().registrarPago(new Pago(MedioPago.TARJETA, 120_000, "Anticipo", false));
        assertEquals(EstadoReserva.CONFIRMADA, servicio.confirmar(r.getCodigo()).getEstado());
    }

    @Test
    void simularLaCancelacionNoCancela() {
        Reserva r = directa(HOY.plusDays(20), HOY.plusDays(22));
        r.getFolio().registrarPago(new Pago(MedioPago.EFECTIVO, 100_000, "Abono", false));
        ResultadoCancelacion simulacion = servicio.simularCancelacion(r.getCodigo());
        assertEquals(100_000, simulacion.devolucion());
        assertTrue(simulacion.regla().startsWith("Más de 7 días"));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void cancelarDevuelveSegunLaPoliticaYLiberaLasNoches() {
        Reserva r = directa(HOY.plusDays(20), HOY.plusDays(22));
        r.getFolio().registrarPago(new Pago(MedioPago.EFECTIVO, 100_000, "Abono", false));
        ResultadoCancelacion resultado = servicio.cancelar(r.getCodigo());
        assertEquals(100_000, resultado.devolucion());
        assertEquals(0, resultado.retencion());
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(2, servicio.consultarDisponibles(HOY.plusDays(20), HOY.plusDays(22), 2).size());
    }

    @Test
    void elCheckOutDejaElApartamentoPendienteDePreparacion() {
        Reserva r = directa(HOY, HOY.plusDays(2));
        servicio.confirmar(r.getCodigo());
        alojamiento.hacerCheckIn(r.getCodigo(), HOY.atTime(15, 0)); // hora fija: la prueba no depende del reloj
        assertEquals(EstadoReserva.FINALIZADA, servicio.hacerCheckOut(r.getCodigo()).getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, r.getApartamento().getEstado());
    }

    @Test
    void unaExternaQueChocaQuedaComoConflicto() {
        directa(HOY.plusDays(1), HOY.plusDays(4));
        assertThrows(ReglaNegocioException.class, () -> servicio.recibirExterna("1094", "APT-101",
                HOY.plusDays(2), HOY.plusDays(3), adultos(1), "BK-1", "Booking"));
        assertEquals(1, servicio.conflictos().size());
    }

    @Test
    void elJobCancelaLasPendientesVencidas() {
        directa(HOY.plusDays(5), HOY.plusDays(6));
        assertEquals(1, servicio.cancelarPendientesVencidas(LocalDateTime.now().plusHours(25)));
    }

    @Test
    void elHuespedVeSoloSusReservas() {
        directa(HOY.plusDays(1), HOY.plusDays(2));
        assertEquals(1, servicio.reservasDelHuesped("1094").size());
        assertEquals(0, servicio.reservasDelHuesped("otro").size());
    }
}
