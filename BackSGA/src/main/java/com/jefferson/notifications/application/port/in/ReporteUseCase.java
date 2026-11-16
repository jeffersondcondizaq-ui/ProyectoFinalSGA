package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Puerto de entrada: reportes del administrador. */
public interface ReporteUseCase {

    Map<Apartamento, Integer> ocupacion(LocalDate desde, LocalDate hasta);

    long ingresos(LocalDate desde, LocalDate hasta);

    Map<Canal, Integer> reservasPorCanal();

    List<Reserva> saldosPendientes();

    List<Apartamento> apartamentosConMasNovedades();
}
