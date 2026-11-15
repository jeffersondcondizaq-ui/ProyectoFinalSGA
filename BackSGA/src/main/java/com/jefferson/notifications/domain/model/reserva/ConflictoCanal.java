package com.jefferson.notifications.domain.model.reserva;


import java.time.LocalDateTime;

public class ConflictoCanal {
    private String codigoExterno;
    private LocalDateTime fechaRegistro;
    private Reserva reservaExistente;
    private String descripcion;

    public ConflictoCanal(String codigoExterno, LocalDateTime fechaRegistro, String descripcion, Reserva reservaExistente) {
        this.codigoExterno = codigoExterno;
        this.fechaRegistro = fechaRegistro;
        this.descripcion = descripcion;
        this.reservaExistente = reservaExistente;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public Reserva getReservaExistente() {
        return reservaExistente;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
