package com.jefferson.notifications.domain.model.apartamento;

import com.jefferson.notifications.domain.model.persona.Recepcionista;

import java.time.LocalDateTime;

public class Novedad {
    private LocalDateTime fecha;
    private TipoNovedad tipo;
    private String descripcion;
    private Recepcionista reportadaPor;

    public Novedad(TipoNovedad tipo, String descripcion, Recepcionista reportadaPor) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.reportadaPor = reportadaPor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoNovedad getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Recepcionista getReportadaPor() {
        return reportadaPor;
    }
}
