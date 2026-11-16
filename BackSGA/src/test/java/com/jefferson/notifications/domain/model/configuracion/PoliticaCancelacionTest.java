package com.jefferson.notifications.domain.model.configuracion;

import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PoliticaCancelacion (7 días / 48 horas / 50 %) y PeriodoTemporada. */
class PoliticaCancelacionTest {

    private final PoliticaCancelacion politica = new PoliticaCancelacion();
    private final LocalDate entrada = LocalDate.of(2026, 12, 20);

    /** Momento a "horas" horas de la entrada (3:00 p. m.). */
    private LocalDateTime antes(int horas) {
        return entrada.atTime(15, 0).minusHours(horas);
    }

    @Test
    void conMasDeSieteDiasNoRetieneNada() {
        assertEquals(0, politica.calcularRetencion(1_000_000, entrada, antes(8 * 24)));
    }

    @Test
    void entreDosYSieteDiasRetieneLaMitad() {
        assertEquals(500_000, politica.calcularRetencion(1_000_000, entrada, antes(5 * 24)));
        assertEquals(500_000, politica.calcularRetencion(1_000_000, entrada, antes(48)));
    }

    @Test
    void conMenosDe48HorasRetieneTodo() {
        assertEquals(1_000_000, politica.calcularRetencion(1_000_000, entrada, antes(47)));
        assertEquals(1_000_000, politica.calcularRetencion(1_000_000, entrada, antes(-5))); // ya pasó la entrada
    }

    @Test
    void laDevolucionNuncaEsNegativa() {
        // Pagó 300.000 y se retienen 500.000: no se devuelve nada
        assertEquals(0, politica.calcularDevolucion(300_000, 1_000_000, entrada, antes(5 * 24)));
        assertEquals(300_000, politica.calcularDevolucion(300_000, 1_000_000, entrada, antes(8 * 24)));
    }

    @Test
    void copiarCreaUnaPoliticaIndependiente() {
        PoliticaCancelacion copia = politica.copiar();
        politica.setPorcentajeRetencionParcial(80);
        assertNotSame(politica, copia);
        assertEquals(50, copia.getPorcentajeRetencionParcial());
    }

    @Test
    void elPorcentajeQuedaEntreCeroYCien() {
        politica.setPorcentajeRetencionParcial(150);
        assertEquals(100, politica.getPorcentajeRetencionParcial());
        politica.setPorcentajeRetencionParcial(-3);
        assertEquals(0, politica.getPorcentajeRetencionParcial());
    }

    @Test
    void unPeriodoContieneSusNochesYDetectaCruces() {
        PeriodoTemporada navidad = new PeriodoTemporada(TipoTemporada.ALTA, LocalDate.of(2026, 12, 15),
                LocalDate.of(2027, 1, 16), "Vacaciones");
        assertTrue(navidad.contieneNoche(LocalDate.of(2026, 12, 15)));
        assertFalse(navidad.contieneNoche(LocalDate.of(2027, 1, 16)));
        assertTrue(navidad.seCruzaCon(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 16)));
        assertFalse(navidad.seCruzaCon(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15)));
    }
}
