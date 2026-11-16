package com.jefferson.notifications.infrastructure.adapter.in.scheduler;

import com.jefferson.notifications.application.port.in.ReservaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VencimientoReservasJob {

    private static final Logger log = LoggerFactory.getLogger(VencimientoReservasJob.class);

    private final ReservaUseCase reservaUseCase;

    public VencimientoReservasJob(ReservaUseCase reservaUseCase) {
        this.reservaUseCase = reservaUseCase;
    }

    @Scheduled(fixedRate = 60_000)
    public void cancelarPendientesVencidas() {
        int canceladas = reservaUseCase.cancelarPendientesVencidas(LocalDateTime.now());
        if (canceladas > 0) {
            log.info("Se cancelaron {} reservas pendientes por vencimiento (24 h)", canceladas);
        }
    }
}