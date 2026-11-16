package com.jefferson.notifications.domain.model.persona;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Persona y Ocupante: la edad siempre se calcula desde la fecha de nacimiento. */
class PersonaTest {

    @Test
    void calculaLaEdadAUnaFecha() {
        Ocupante ana = new Ocupante("Ana", LocalDate.of(2000, 5, 20));
        assertEquals(25, ana.calcularEdad(LocalDate.of(2026, 5, 19)));
        assertEquals(26, ana.calcularEdad(LocalDate.of(2026, 5, 20)));
    }

    @Test
    void laEdadNuncaEsNegativa() {
        Ocupante bebe = new Ocupante("Bebé", LocalDate.of(2030, 1, 1));
        assertEquals(0, bebe.calcularEdad(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void pagaElOcupanteDeSeisAniosOMas() {
        LocalDate entrada = LocalDate.of(2026, 12, 20);
        assertTrue(new Ocupante("Seis justos", entrada.minusYears(6)).esPagador(entrada));
        assertTrue(new Ocupante("Adulto", LocalDate.of(1990, 1, 1)).esPagador(entrada));
    }

    @Test
    void noPagaElMenorDeSeisAnios() {
        LocalDate entrada = LocalDate.of(2026, 12, 20);
        assertFalse(new Ocupante("Casi seis", entrada.minusYears(6).plusDays(1)).esPagador(entrada));
        assertFalse(new Ocupante("Bebé", entrada.minusMonths(8)).esPagador(entrada));
    }
}
