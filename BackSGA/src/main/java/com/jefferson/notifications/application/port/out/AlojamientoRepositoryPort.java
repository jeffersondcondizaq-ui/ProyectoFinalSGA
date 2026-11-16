package com.jefferson.notifications.application.port.out;

import com.jefferson.notifications.domain.model.Alojamiento;

public interface AlojamientoRepositoryPort {

    Alojamiento obtener();

    void guardar(Alojamiento alojamiento);
}