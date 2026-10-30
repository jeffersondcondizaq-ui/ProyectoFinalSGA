package domain.model.reserva;

import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EstadoReservaTest {

    private static final Set<String> PERMITIDAS = Set.of(
            "PENDIENTE->CONFIRMADA",
            "PENDIENTE->CANCELADA",
            "CONFIRMADA->EN_CURSO",
            "CONFIRMADA->CANCELADA",
            "CONFIRMADA->NO_SHOW",
            "EN_CURSO->FINALIZADA"
    );

    @ParameterizedTest(name = "{0} -> {1} está permitida")
    @CsvSource({
            "PENDIENTE, CONFIRMADA",
            "PENDIENTE, CANCELADA",
            "CONFIRMADA, EN_CURSO",
            "CONFIRMADA, CANCELADA",
            "CONFIRMADA, NO_SHOW",
            "EN_CURSO, FINALIZADA"
    })
    void transicionesPermitidas(EstadoReserva actual, EstadoReserva nuevo) {
        assertTrue(actual.puedePasarA(nuevo));
    }

    @Test
    void cualquierOtraTransicionSeRechaza() {
        for (EstadoReserva actual : EstadoReserva.values()) {
            for (EstadoReserva nuevo : EstadoReserva.values()) {
                boolean esperada = PERMITIDAS.contains(actual + "->" + nuevo);
                assertEquals(esperada, actual.puedePasarA(nuevo),
                        () -> actual + " -> " + nuevo);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"FINALIZADA", "CANCELADA", "NO_SHOW"})
    void losEstadosFinalesNoTienenSalida(EstadoReserva finalState) {
        for (EstadoReserva nuevo : EstadoReserva.values()) {
            assertFalse(finalState.puedePasarA(nuevo));
        }
    }

    @Test
    void puedePasarANullDevuelveFalse() {
        assertFalse(EstadoReserva.PENDIENTE.puedePasarA(null));
    }

    @Test
    void esActivoSoloParaPendienteConfirmadaYEnCurso() {
        Set<EstadoReserva> activos = EnumSet.of(
                EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);
        for (EstadoReserva estado : EstadoReserva.values()) {
            assertEquals(activos.contains(estado), estado.esActivo(), estado::name);
        }
    }
}