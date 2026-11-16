package com.jefferson.notifications.domain.model.comun;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bloqueo, PeriodoTemporada y Reserva cumplen el mismo contrato de rango [inicio, fin). */
class IRangoFechasTest {

    private final LocalDate inicio = LocalDate.of(2026, 12, 10);
    private final LocalDate fin = inicio.plusDays(3);

    private List<IRangoFechas> rangos() {
        Huesped laura = new Huesped("Laura", LocalDate.of(1995, 7, 18), "1094", "320", "l@x.co", "laura", "secreta");
        Apartamento apto = new Apartamento("APT-101", "Suite", 2, 4, new ArrayList<>(), EstadoApartamento.PREPARADO,
                null, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        return List.of(
                new Bloqueo(inicio, fin, "Pintura"),
                new PeriodoTemporada(TipoTemporada.ALTA, inicio, fin, "Puente"),
                new ReservaDirecta("R-1", laura, apto, inicio, fin, new ArrayList<>(), null, null));
    }

    @Test
    void contienenDesdeElInicioHastaAntesDelFin() {
        for (IRangoFechas rango : rangos()) {
            assertTrue(rango.contieneNoche(inicio));
            assertTrue(rango.contieneNoche(fin.minusDays(1)));
            assertFalse(rango.contieneNoche(fin));
            assertFalse(rango.contieneNoche(inicio.minusDays(1)));
        }
    }

    @Test
    void seCruzanSoloSiCompartenAlMenosUnaNoche() {
        for (IRangoFechas rango : rangos()) {
            assertTrue(rango.seCruzaCon(fin.minusDays(1), fin.plusDays(2)));
            assertFalse(rango.seCruzaCon(fin, fin.plusDays(2)));
            assertFalse(rango.seCruzaCon(inicio.minusDays(2), inicio));
            assertFalse(rango.seCruzaCon(null, fin));
        }
    }
}
