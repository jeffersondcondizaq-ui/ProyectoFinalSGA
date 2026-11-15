package com.jefferson.notifications.domain.model.folio;


public class Cargo extends MovimientoFolio {

    private TipoCargo tipo;


    public Cargo( TipoCargo tipo, long valor, String descripcion, boolean esCorreccion) {
        super(valor, descripcion, esCorreccion);
        this.tipo = tipo;
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
        return new Cargo(tipo, valor, "Corrección de: " + descripcion, true);
    }

    public TipoCargo getTipo() {
        return tipo;
    }
}