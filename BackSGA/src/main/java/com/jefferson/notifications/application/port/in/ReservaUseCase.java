package com.jefferson.notifications.application.port.in;

import java.time.LocalDateTime;

public interface ReservaUseCase {
    boolean confirmarReserva(String codigo);

    int cancelarPendientesVencidas(LocalDateTime ahora);
}
