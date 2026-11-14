package com.jefferson.notifications.domain.model.configuracion;

import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.comun.IRangoFechas;
import java.time.LocalDate;

public class PeriodoTemporada implements IRangoFechas {
    private TipoTemporada tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String descripcion;

    public PeriodoTemporada(TipoTemporada tipo, LocalDate fechaInicio, LocalDate fechaFin, String descripcion) {
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.descripcion = descripcion;
    }

    @Override
    public boolean contieneNoche(LocalDate noche) {
        if (noche == null) {
            return false;
        }

        boolean desdeElInicio = noche.isEqual(fechaInicio) || noche.isAfter(fechaInicio);
        boolean antesDelFin = noche.isBefore(fechaFin);

        if (desdeElInicio && antesDelFin) {
            return true;
        }
        return false;
    }

    @Override
    public boolean seCruzaCon(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            return false;
        }

        if (fechaInicio.isBefore(salida) && entrada.isBefore(fechaFin)) {
            return true;
        }
        return false;
    }

    public TipoTemporada getTipo() {
        return tipo;
    }

    @Override
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    @Override
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
