package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;

import java.util.List;

/** Puerto de entrada: periodos de temporada y política de cancelación. */
public interface ConfiguracionUseCase {

    List<PeriodoTemporada> listarTemporadas();

    PeriodoTemporada registrarTemporada(PeriodoTemporada periodo);

    PoliticaCancelacion obtenerPolitica();

    PoliticaCancelacion configurarPolitica(PoliticaCancelacion politica);
}
