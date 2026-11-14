package com.jefferson.notifications.domain.model.comun;
import java.time.LocalDate;

public interface IRangoFechas {
    LocalDate getFechaInicio();

    LocalDate getFechaFin();

    boolean contieneNoche(LocalDate noche);

    boolean seCruzaCon(LocalDate entrada, LocalDate salida);

}
