package com.jefferson.notifications.domain.model.apartamento;

import java.time.LocalDate;

public class Bloqueo {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;

    public Bloqueo(LocalDate fechaInicio, LocalDate fechaFin, String motivo) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
    }


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

    public boolean seCruzaCon(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            return false;
        }

        if (fechaInicio.isBefore(salida) && entrada.isBefore(fechaFin)) {
            return true;
        }
        return false;
    }


    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }
}