package com.jefferson.notifications.infrastructure.config;

import com.jefferson.notifications.application.port.in.AutenticacionUseCase;
import com.jefferson.notifications.application.port.in.ConfiguracionUseCase;
import com.jefferson.notifications.application.port.in.FolioUseCase;
import com.jefferson.notifications.application.port.in.GestionApartamentoUseCase;
import com.jefferson.notifications.application.port.in.GestionEmpleadoUseCase;
import com.jefferson.notifications.application.port.in.GestionHuespedUseCase;
import com.jefferson.notifications.application.port.in.ReporteUseCase;
import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.application.service.ApartamentoService;
import com.jefferson.notifications.application.service.AutenticacionService;
import com.jefferson.notifications.application.service.ConfiguracionService;
import com.jefferson.notifications.application.service.EmpleadoService;
import com.jefferson.notifications.application.service.FolioService;
import com.jefferson.notifications.application.service.HuespedService;
import com.jefferson.notifications.application.service.ReporteService;
import com.jefferson.notifications.application.service.ReservaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aquí se conectan los casos de uso (application) con los adaptadores (infrastructure).
 * Spring le pasa el AlojamientoMemoriaAdapter porque es el @Component que implementa el puerto.
 */
@Configuration
public class BeanConfig {

    @Bean
    public AutenticacionUseCase autenticacionUseCase(AlojamientoRepositoryPort repositorio) {
        return new AutenticacionService(repositorio);
    }

    @Bean
    public GestionApartamentoUseCase gestionApartamentoUseCase(AlojamientoRepositoryPort repositorio) {
        return new ApartamentoService(repositorio);
    }

    @Bean
    public ReservaUseCase reservaUseCase(AlojamientoRepositoryPort repositorio) {
        return new ReservaService(repositorio);
    }

    @Bean
    public FolioUseCase folioUseCase(AlojamientoRepositoryPort repositorio) {
        return new FolioService(repositorio);
    }

    @Bean
    public GestionHuespedUseCase gestionHuespedUseCase(AlojamientoRepositoryPort repositorio) {
        return new HuespedService(repositorio);
    }

    @Bean
    public GestionEmpleadoUseCase gestionEmpleadoUseCase(AlojamientoRepositoryPort repositorio) {
        return new EmpleadoService(repositorio);
    }

    @Bean
    public ConfiguracionUseCase configuracionUseCase(AlojamientoRepositoryPort repositorio) {
        return new ConfiguracionService(repositorio);
    }

    @Bean
    public ReporteUseCase reporteUseCase(AlojamientoRepositoryPort repositorio) {
        return new ReporteService(repositorio);
    }
}
