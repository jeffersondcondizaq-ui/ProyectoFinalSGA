package com.jefferson.notifications.domain.model.folio;

import java.time.LocalDateTime;

public abstract class MovimientoFolio {

    protected final LocalDateTime fecha;
    protected final long valor;
    protected final String descripcion;
    protected final boolean esCorreccion;

    public MovimientoFolio(long valor, String descripcion, boolean esCorreccion) {
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
        this.esCorreccion = esCorreccion;

        if (valor < 0) {
            this.valor = 0;
        } else {
            this.valor = valor;
        }
    }


    public abstract long getEfectoSaldo();

    public abstract MovimientoFolio crearCorreccion();

    public LocalDateTime getFecha() {
        return fecha;
    }

    public long getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsCorreccion() {
        return esCorreccion;
    }
}