package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.Alojamiento;

import java.time.LocalDateTime;

public class ReservaService implements ReservaUseCase {
    private final AlojamientoRepositoryPort repositorio;

    public ReservaService(AlojamientoRepositoryPort repositorio){
        this.repositorio = repositorio;
    }

    @Override
    public synchronized  boolean confirmarReserva(String codigo){
        Alojamiento alojamiento = repositorio.obtener();
        boolean confirmar = alojamiento.confirmarReserva(codigo);
        repositorio.guardar(alojamiento);
        return confirmar;
    }
    @Override
    public synchronized int cancelarPendientesVencidas(LocalDateTime ahora){
        Alojamiento alojamiento = repositorio.obtener();
        int canceladas = alojamiento.cancelarPendientesVencidas(ahora);
        repositorio.guardar(alojamiento);
        return canceladas;
    }
}
