package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;

import java.time.LocalDate;
import java.util.ArrayList;

public class ReservaExterna extends Reserva {

    private static final int PORCENTAJE_COMISION = 15;

    private String codigoExterno;
    private String plataforma;

    public ReservaExterna(String codigo, Huesped titular, Apartamento apartamento, LocalDate fechaEntrada,
                          LocalDate fechaSalida, ArrayList<Ocupante> ocupantes,
                          ArrayList<DetalleNoche> detalleNoches, PoliticaCancelacion politica,
                          String codigoExterno, String plataforma) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, detalleNoches, politica);
        this.codigoExterno = codigoExterno;
        this.plataforma = plataforma;
    }

    @Override
    public boolean puedeConfirmarse() {
        return true;
    }

    @Override
    public Canal getCanal() {
        return Canal.EXTERNA;
    }

    @Override
    public long calcularComision() {
        return valorEstancia * PORCENTAJE_COMISION / 100;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public String getPlataforma() {
        return plataforma;
    }
}