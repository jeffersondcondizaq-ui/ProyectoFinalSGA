package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoNovedad;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.jefferson.notifications.application.service.AlojamientoDePrueba.HOY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApartamentoServiceTest {

    private ApartamentoService servicio;
    private Alojamiento alojamiento;

    @BeforeEach
    void preparar() {
        AlojamientoRepositoryPort repositorio = mock(AlojamientoRepositoryPort.class);
        alojamiento = AlojamientoDePrueba.crear();
        when(repositorio.obtener()).thenReturn(alojamiento);
        servicio = new ApartamentoService(repositorio);
    }

    @Test
    void elCatalogoNoMuestraLosEliminados() {
        servicio.eliminar("APT-201");
        assertEquals(1, servicio.listar(false).size());
        assertEquals(2, servicio.listar(true).size());
    }

    @Test
    void actualizaSoloLosDatosEnviados() {
        servicio.actualizar("APT-101", null, 3, null, List.of("Jacuzzi"));
        assertEquals("Apto APT-101", servicio.buscar("APT-101").getNombre());
        assertEquals(3, servicio.buscar("APT-101").getNumDormitorios());
        assertEquals(2, servicio.buscar("APT-101").getCapacidadMaxima());
    }

    @Test
    void defineTarifasPositivas() {
        servicio.definirTarifas("APT-101", Map.of(TipoTemporada.ALTA, 250_000L));
        assertEquals(250_000, servicio.buscar("APT-101").getTarifa(TipoTemporada.ALTA));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.definirTarifas("APT-101", Map.of(TipoTemporada.BAJA, 0L)));
    }

    @Test
    void elEstadoOcupadoNoSeAsignaAMano() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado("APT-101", EstadoApartamento.OCUPADO, null));
    }

    @Test
    void fueraDeServicioExigeMotivo() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado("APT-101", EstadoApartamento.FUERA_DE_SERVICIO, null));
        servicio.cambiarEstado("APT-101", EstadoApartamento.FUERA_DE_SERVICIO, "Mantenimiento eléctrico");
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, servicio.buscar("APT-101").getEstado());
    }

    @Test
    void unBloqueoQuitaElApartamentoDeLaDisponibilidad() {
        servicio.registrarBloqueo("APT-101", new Bloqueo(HOY.plusDays(1), HOY.plusDays(3), "Pintura"));
        assertEquals(1, alojamiento.consultarDisponibles(HOY.plusDays(1), HOY.plusDays(2), 1).size());
    }

    @Test
    void lasNovedadesNecesitanDescripcion() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarNovedad("APT-101", TipoNovedad.DANIO, " ", null));
        servicio.registrarNovedad("APT-101", TipoNovedad.DANIO, "Vidrio roto", null);
        assertEquals(1, servicio.buscar("APT-101").getNovedades().size());
    }
}
