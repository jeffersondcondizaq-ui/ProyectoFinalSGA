package com.jefferson.notifications.domain.model.apartamento;

public class Tarifa {

    private TipoTemporada tipoTemporada;
    private long valorPersonaNoche;

    public Tarifa(TipoTemporada tipoTemporada, long valorPersonaNoche) {
        this.tipoTemporada = tipoTemporada;

        if (valorPersonaNoche < 0) {
            this.valorPersonaNoche = 0;
        } else {
            this.valorPersonaNoche = valorPersonaNoche;
        }
    }

    public TipoTemporada getTipoTemporada() {
        return tipoTemporada;
    }

    public void setTipoTemporada(TipoTemporada tipoTemporada) {
        this.tipoTemporada = tipoTemporada;
    }

    public long getValorPersonaNoche() {
        return valorPersonaNoche;
    }

    public void setValorPersonaNoche(long valorPersonaNoche) {
        if (valorPersonaNoche < 0) {
            this.valorPersonaNoche = 0;
        } else {
            this.valorPersonaNoche = valorPersonaNoche;
        }
    }
}
