package com.jefferson.notifications.domain.model.folio;


public class Devolucion extends MovimientoFolio {

    public Devolucion(long valor, String descripcion, boolean esCorreccion) {
        super(valor, descripcion, esCorreccion);
    }

    @Override
    public long getEfectoSaldo() {
        if (esCorreccion) {
            return -valor;
        }
        return valor;
    }

    @Override
    public MovimientoFolio crearCorreccion() {
        return new Devolucion(valor, "Corrección de: " + descripcion, true);
    }
}