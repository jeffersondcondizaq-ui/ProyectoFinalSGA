package com.jefferson.notifications.domain.model.folio;

import java.util.ArrayList;


public class Folio {

    private ArrayList<MovimientoFolio> movimientos;

    public Folio() {
        this.movimientos = new ArrayList<>();
    }

    public boolean agregarCargo(Cargo cargo) {
        return agregarMovimiento(cargo);
    }

    public boolean registrarPago(Pago pago) {
        return agregarMovimiento(pago);
    }

    public boolean registrarDevolucion(Devolucion devolucion) {
        return agregarMovimiento(devolucion);
    }


    private boolean agregarMovimiento(MovimientoFolio movimiento) {
        if (movimiento == null) {
            return false;
        }
        if (movimiento.getValor() <= 0) {
            return false;
        }
        movimientos.add(movimiento);
        return true;
    }


    public boolean corregir(MovimientoFolio movimiento) {
        if (movimiento == null) {
            return false;
        }


        if (movimiento.isEsCorreccion()) {
            return false;
        }

        boolean estaEnElFolio = false;
        for (int i = 0; i < movimientos.size(); i++) {
            if (movimientos.get(i) == movimiento) {
                estaEnElFolio = true;
            }
        }
        if (!estaEnElFolio) {
            return false;
        }

        MovimientoFolio correccion = movimiento.crearCorreccion();
        movimientos.add(correccion);
        return true;
    }

    public long calcularTotalCargos() {
        long total = 0;
        for (int i = 0; i < movimientos.size(); i++) {
            MovimientoFolio movimiento = movimientos.get(i);
            if (movimiento instanceof Cargo) {
                if (movimiento.isEsCorreccion()) {
                    total = total - movimiento.getValor();
                } else {
                    total = total + movimiento.getValor();
                }
            }
        }
        return total;
    }

    public long calcularTotalPagos() {
        long total = 0;
        for (int i = 0; i < movimientos.size(); i++) {
            MovimientoFolio movimiento = movimientos.get(i);

            if (movimiento instanceof Pago) {
                if (movimiento.isEsCorreccion()) {
                    total = total - movimiento.getValor();
                } else {
                    total = total + movimiento.getValor();
                }
            }

            if (movimiento instanceof Devolucion) {
                if (movimiento.isEsCorreccion()) {
                    total = total + movimiento.getValor();
                } else {
                    total = total - movimiento.getValor();
                }
            }
        }
        return total;
    }

    public long calcularSaldo() {
        long saldo = 0;
        for (int i = 0; i < movimientos.size(); i++) {
            saldo = saldo + movimientos.get(i).getEfectoSaldo();
        }
        return saldo;
    }

    public ArrayList<MovimientoFolio> getMovimientos() {
        return movimientos;
    }
}