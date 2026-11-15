package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;

import java.time.LocalDate;
import java.util.ArrayList;


public class ReservaDirecta extends Reserva {

    public ReservaDirecta(String codigo, Huesped titular, Apartamento apartamento, LocalDate fechaEntrada,
                          LocalDate fechaSalida, ArrayList<Ocupante> ocupantes,
                          ArrayList<DetalleNoche> detalleNoches, PoliticaCancelacion politica) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, detalleNoches, politica);
    }

    @Override
    public boolean puedeConfirmarse() {
        return true;
    }

    @Override
    public Canal getCanal() {
        return Canal.DIRECTA;
    }
}