package com.jefferson.notifications.infrastructure.config;

import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.application.port.in.AutenticacionUseCase;
import com.jefferson.notifications.application.service.AutenticacionService;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.application.service.ReservaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public ReservaUseCase reservaUseCase(AlojamientoRepositoryPort repositorio) {
        return new ReservaService(repositorio);
    }
    @Bean
    public AutenticacionUseCase autenticacionUseCase(AlojamientoRepositoryPort repositorio) {
        return new AutenticacionService(repositorio);
    }
}
