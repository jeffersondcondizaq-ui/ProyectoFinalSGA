package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Recepcionista;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.jefferson.notifications.application.service.AlojamientoDePrueba.HOY;
import static com.jefferson.notifications.application.service.AlojamientoDePrueba.adultos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** EmpleadoService, HuespedService, ConfiguracionService y ReporteService. */
class AdministracionServiceTest {

    private AlojamientoRepositoryPort repositorio;

    @BeforeEach
    void preparar() {
        repositorio = mock(AlojamientoRepositoryPort.class);
        Alojamiento alojamiento = AlojamientoDePrueba.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
    }

    @Test
    void elAdministradorContrataEditaYDesvincula() {
        EmpleadoService empleados = new EmpleadoService(repositorio);
        empleados.contratar(new Recepcionista("Paula", LocalDate.of(1996, 1, 22), "3", "315", "p@x.co", "paula",
                "paula123", HOY));
        assertEquals(3, empleados.listar().size());
        assertEquals("316", empleados.actualizar("3", "316", "nuevo@x.co").getTelefono());
        empleados.desvincular("3");
        assertFalse(empleados.listar().get(2).isActivo());
    }

    @Test
    void noSeContrataConUnUsuarioRepetido() {
        assertThrows(ReglaNegocioException.class, () -> new EmpleadoService(repositorio).contratar(
                new Recepcionista("Otro", LocalDate.of(1990, 1, 1), "9", "3", "o@x.co", "admin", "123456", HOY)));
    }

    @Test
    void recepcionRegistraHuespedesYElHuespedEditaSuPerfil() {
        HuespedService huespedes = new HuespedService(repositorio);
        huespedes.registrarEnRecepcion(new Huesped("Carlos", LocalDate.of(1988, 11, 23), "777", "315", "c@x.co",
                "777", "777"));
        assertEquals("Carlos", huespedes.buscar("777").getNombre());
        assertEquals("399", huespedes.actualizarPerfil("777", "399", "c2@x.co", null).getTelefono());
        assertThrows(ReglaNegocioException.class, () -> huespedes.actualizarPerfil("777", "", "c@x.co", null));
    }

    @Test
    void temporadasSinCrucesYPoliticaValida() {
        ConfiguracionService configuracion = new ConfiguracionService(repositorio);
        configuracion.registrarTemporada(new PeriodoTemporada(TipoTemporada.ALTA, HOY.plusDays(10), HOY.plusDays(20),
                "Alta"));
        assertThrows(ReglaNegocioException.class, () -> configuracion.registrarTemporada(
                new PeriodoTemporada(TipoTemporada.MEDIA, HOY.plusDays(15), HOY.plusDays(25), "Cruza")));
        assertThrows(ReglaNegocioException.class,
                () -> configuracion.configurarPolitica(new PoliticaCancelacion(1, 72, 50)));
        configuracion.configurarPolitica(new PoliticaCancelacion(10, 48, 30));
        assertEquals(30, configuracion.obtenerPolitica().getPorcentajeRetencionParcial());
    }

    @Test
    void losReportesReflejanLasReservas() {
        ReservaService reservas = new ReservaService(repositorio);
        Reserva r = reservas.crearDirecta("1094", "APT-101", HOY.plusDays(1), HOY.plusDays(3), adultos(2));
        new FolioService(repositorio).registrarPago(r.getCodigo(), MedioPago.EFECTIVO, 100_000, "Abono");
        reservas.confirmar(r.getCodigo());

        ReporteService reportes = new ReporteService(repositorio);
        assertEquals(1, reportes.reservasPorCanal().get(Canal.DIRECTA).intValue());
        assertEquals(100_000, reportes.ingresos(HOY, HOY));
        assertEquals(1, reportes.saldosPendientes().size());
        assertEquals(2, reportes.ocupacion(HOY, HOY.plusDays(10)).get(r.getApartamento()).intValue());
    }
}
