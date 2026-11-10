package com.jefferson.notifications.domain.model.persona;

import java.time.LocalDate;

public abstract class Persona {

    protected String nombre;
    protected LocalDate fechaNacimiento;

    public Persona(String nombre, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    // Metodos
    public int calcularEdad(LocalDate fecha) {
        int edad = fecha.getYear() - fechaNacimiento.getYear();

        if (fecha.getMonthValue() < fechaNacimiento.getMonthValue()) {
            edad = edad - 1;
        } else if (fecha.getMonthValue() == fechaNacimiento.getMonthValue()
                && fecha.getDayOfMonth() < fechaNacimiento.getDayOfMonth()) {
            edad = edad - 1;
        }

        if (edad < 0) {
            edad = 0;
        }
        return edad;
    }

}
