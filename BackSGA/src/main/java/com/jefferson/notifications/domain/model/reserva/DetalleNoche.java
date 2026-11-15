package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;

import java.time.LocalDate;

public class DetalleNoche {

    private LocalDate fecha;
    private TipoTemporada temporada;
    private long tarifaPersona;
    private int pagadores;

    public DetalleNoche(LocalDate fecha, TipoTemporada temporada, long tarifaPersona, int pagadores) {
        this.fecha = fecha;
        this.temporada = temporada;

        if (tarifaPersona < 0) {
            this.tarifaPersona = 0;
        } else {
            this.tarifaPersona = tarifaPersona;
        }

        if (pagadores < 0) {
            this.pagadores = 0;
        } else {
            this.pagadores = pagadores;
        }
    }

    public long calcularSubtotal() {
        return tarifaPersona * pagadores;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoTemporada getTemporada() {
        return temporada;
    }

    public long getTarifaPersona() {
        return tarifaPersona;
    }

    public int getPagadores() {
        return pagadores;
    }
}