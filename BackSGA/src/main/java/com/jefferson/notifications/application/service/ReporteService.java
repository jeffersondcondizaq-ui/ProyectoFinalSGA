package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.ReporteUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReporteService extends ServicioBase implements ReporteUseCase {

    public ReporteService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public Map<Apartamento, Integer> ocupacion(LocalDate desde, LocalDate hasta) {
        return ejecutar(alojamiento -> alojamiento.reporteOcupacion(desde, hasta));
    }

    @Override
    public long ingresos(LocalDate desde, LocalDate hasta) {
        return ejecutar(alojamiento -> alojamiento.reporteIngresos(desde, hasta));
    }

    @Override
    public Map<Canal, Integer> reservasPorCanal() {
        return ejecutar(alojamiento -> alojamiento.reporteReservasPorCanal());
    }

    @Override
    public List<Reserva> saldosPendientes() {
        return ejecutar(alojamiento -> alojamiento.reporteSaldosPendientes());
    }

    @Override
    public List<Apartamento> apartamentosConMasNovedades() {
        return ejecutar(alojamiento -> alojamiento.reporteApartamentosConMasNovedades());
    }
}
