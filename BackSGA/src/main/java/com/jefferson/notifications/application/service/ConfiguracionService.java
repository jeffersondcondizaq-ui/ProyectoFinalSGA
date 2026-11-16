package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.ConfiguracionUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;

import java.util.ArrayList;
import java.util.List;

public class ConfiguracionService extends ServicioBase implements ConfiguracionUseCase {

    public ConfiguracionService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public List<PeriodoTemporada> listarTemporadas() {
        return ejecutar(alojamiento -> new ArrayList<>(alojamiento.getPeriodosTemporada()));
    }

    @Override
    public PeriodoTemporada registrarTemporada(PeriodoTemporada periodo) {
        return ejecutar(alojamiento -> {
            alojamiento.registrarPeriodoTemporada(periodo);
            return periodo;
        });
    }

    @Override
    public PoliticaCancelacion obtenerPolitica() {
        return ejecutar(alojamiento -> alojamiento.getPoliticaCancelacion());
    }

    @Override
    public PoliticaCancelacion configurarPolitica(PoliticaCancelacion politica) {
        if (politica.getHorasRetencionTotal() > politica.getDiasDevolucionTotal() * 24
                || politica.getPorcentajeRetencionParcial() < 0 || politica.getPorcentajeRetencionParcial() > 100) {
            throw new ReglaNegocioException("La política no es válida: las horas de retención total deben ser "
                    + "menores que los días de devolución total y el porcentaje debe estar entre 0 y 100");
        }
        return ejecutar(alojamiento -> {
            alojamiento.configurarPolitica(politica);
            return politica;
        });
    }
}
