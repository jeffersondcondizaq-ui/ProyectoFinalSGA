package com.jefferson.notifications.domain.model.folio;


public class Pago extends MovimientoFolio {

    private MedioPago medio;

    public Pago(MedioPago medio, long valor, String descripcion, boolean esCorreccion) {
        super(valor, descripcion, esCorreccion);
        this.medio = medio;
    }

    @Override
    public long getEfectoSaldo() {
        if (esCorreccion) {
            return valor;
        }
        return -valor;
    }

    @Override
    public MovimientoFolio crearCorreccion() {
        return new Pago(medio, valor, "Corrección de: " + descripcion, true);
    }

    public MedioPago getMedio() {
        return medio;
    }
}