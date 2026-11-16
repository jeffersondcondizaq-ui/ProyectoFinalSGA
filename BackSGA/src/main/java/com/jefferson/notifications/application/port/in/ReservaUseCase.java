package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.ConflictoCanal;
import com.jefferson.notifications.domain.model.reserva.DetalleNoche;
import com.jefferson.notifications.domain.model.reserva.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Puerto de entrada: disponibilidad, cotización, los 3 canales y el ciclo de vida de la reserva. */
public interface ReservaUseCase {

    List<Apartamento> consultarDisponibles(LocalDate entrada, LocalDate salida, int personas);

    List<DetalleNoche> cotizar(String codigoApartamento, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes);

    Reserva crearPortal(String documentoTitular, String codigoApartamento, LocalDate entrada, LocalDate salida,
                        List<Ocupante> ocupantes);

    Reserva crearDirecta(String documentoTitular, String codigoApartamento, LocalDate entrada, LocalDate salida,
                         List<Ocupante> ocupantes);

    Reserva recibirExterna(String documentoTitular, String codigoApartamento, LocalDate entrada, LocalDate salida,
                           List<Ocupante> ocupantes, String codigoExterno, String plataforma);

    List<Reserva> listar();

    List<Reserva> reservasDelHuesped(String documento);

    Reserva buscar(String codigo);

    Reserva confirmar(String codigo);

    /** Cuánto se devolvería si se cancela ahora; no cambia nada. */
    ResultadoCancelacion simularCancelacion(String codigo);

    ResultadoCancelacion cancelar(String codigo);

    Reserva hacerCheckIn(String codigo);

    Reserva hacerCheckOut(String codigo);

    Reserva declararNoShow(String codigo);

    List<ConflictoCanal> conflictos();

    int cancelarPendientesVencidas(LocalDateTime ahora);

    record ResultadoCancelacion(long totalPagado, long retencion, long devolucion, String regla) {
    }
}
