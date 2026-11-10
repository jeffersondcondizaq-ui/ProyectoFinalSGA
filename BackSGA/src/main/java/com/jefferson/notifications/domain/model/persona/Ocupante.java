package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public class Ocupante extends Persona {

    private Persona persona;
    private static final int EDAD_MINIMA_PAGO = 6;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        super(nombre, fechaNacimiento);
    }

    public boolean esPagador(LocalDate fechaEntrada) {
        int edad = persona.calcularEdad(fechaEntrada); ;
        if (edad >= EDAD_MINIMA_PAGO) {
            return true;
        }
        return false;
    }
}
