package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;

import java.time.LocalDate;
import java.util.ArrayList;


public class ReservaPortal extends Reserva {

    private static final int PORCENTAJE_ANTICIPO = 30;

    public ReservaPortal(String codigo, Huesped titular, Apartamento apartamento, LocalDate fechaEntrada,
                         LocalDate fechaSalida, ArrayList<Ocupante> ocupantes,
                         ArrayList<DetalleNoche> detalleNoches, PoliticaCancelacion politica) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, detalleNoches, politica);
    }

    public long calcularAnticipo() {
        return valorEstancia * PORCENTAJE_ANTICIPO / 100;
    }

    @Override
    public boolean puedeConfirmarse() {
        long pagado = folio.calcularTotalPagos();
        if (pagado >= calcularAnticipo()) {
            return true;
        }
        return false;
    }

    @Override
    public Canal getCanal() {
        return Canal.PORTAL;
    }
}