package com.jefferson.notifications.domain.model.apartamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Apartamento, Tarifa, Bloqueo y Novedad. */
class ApartamentoTest {

    private Apartamento apartamento;
    private final LocalDate hoy = LocalDate.of(2026, 12, 1);

    @BeforeEach
    void preparar() {
        apartamento = new Apartamento("APT-101", "Suite Cocora", 2, 4, new ArrayList<>(),
                EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void guardaUnaTarifaPorTemporadaYLaReemplazaSiSeRepite() {
        apartamento.definirTarifa(TipoTemporada.BAJA, 80_000);
        apartamento.definirTarifa(TipoTemporada.ALTA, 130_000);
        apartamento.definirTarifa(TipoTemporada.BAJA, 90_000);
        assertEquals(90_000, apartamento.getTarifa(TipoTemporada.BAJA));
        assertEquals(130_000, apartamento.getTarifa(TipoTemporada.ALTA));
        assertEquals(2, apartamento.getTarifas().size());
    }

    @Test
    void sinTarifaDefinidaDevuelveCero() {
        assertEquals(0, apartamento.getTarifa(TipoTemporada.MEDIA));
    }

    @Test
    void admiteHastaSuCapacidadMaxima() {
        assertTrue(apartamento.admiteOcupantes(4));
        assertFalse(apartamento.admiteOcupantes(5));
        assertFalse(apartamento.admiteOcupantes(0));
    }

    @Test
    void unBloqueoCubreSusNochesSinIncluirElDiaFinal() {
        assertTrue(apartamento.agregarBloqueo(new Bloqueo(hoy, hoy.plusDays(2), "Pintura")));
        assertTrue(apartamento.estaBloqueado(hoy));
        assertTrue(apartamento.estaBloqueado(hoy.plusDays(1)));
        assertFalse(apartamento.estaBloqueado(hoy.plusDays(2)));
    }

    @Test
    void rechazaUnBloqueoSinNoches() {
        assertFalse(apartamento.agregarBloqueo(new Bloqueo(hoy, hoy, "Nada")));
        assertFalse(apartamento.agregarBloqueo(null));
    }

    @Test
    void fueraDeServicioExigeMotivoYLoGuarda() {
        assertFalse(apartamento.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, ""));
        assertTrue(apartamento.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Daño en la tubería"));
        assertEquals("Daño en la tubería", apartamento.getMotivoFueraServicio());
        // Al volver a otro estado se borra el motivo
        apartamento.cambiarEstado(EstadoApartamento.EN_PREPARACION, null);
        assertNull(apartamento.getMotivoFueraServicio());
    }

    @Test
    void noRepiteCaracteristicas() {
        apartamento.agregarCaracteristica("Balcón");
        apartamento.agregarCaracteristica("Balcón");
        assertEquals(1, apartamento.getCaracteristicas().size());
    }

    @Test
    void laEliminacionEsLogica() {
        apartamento.desactivar();
        assertFalse(apartamento.isActivo());
        assertEquals("APT-101", apartamento.getCodigo());
    }

    @Test
    void registraNovedades() {
        apartamento.registrarNovedad(new Novedad(TipoNovedad.DANIO, "Vidrio roto", null));
        assertEquals(1, apartamento.getNovedades().size());
        assertEquals(TipoNovedad.DANIO, apartamento.getNovedades().get(0).getTipo());
    }
}
