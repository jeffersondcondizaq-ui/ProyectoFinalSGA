package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.Alojamiento;

import java.util.function.Function;

/**
 * Base de los servicios: obtiene el Alojamiento del puerto de salida, ejecuta la operación
 * bloqueando el Alojamiento (para que dos peticiones o el job no lo modifiquen a la vez)
 * y lo guarda.
 */
public abstract class ServicioBase {

    private final AlojamientoRepositoryPort repositorio;

    protected ServicioBase(AlojamientoRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    protected <T> T ejecutar(Function<Alojamiento, T> operacion) {
        Alojamiento alojamiento = repositorio.obtener();
        synchronized (alojamiento) {
            T resultado = operacion.apply(alojamiento);
            repositorio.guardar(alojamiento);
            return resultado;
        }
    }
}
