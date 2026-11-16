package com.jefferson.notifications.application.service;

import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.persona.Administrador;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.persona.Recepcionista;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Alojamiento pequeño para las pruebas de los servicios: 2 apartamentos, 1 huésped y 2 empleados. */
final class AlojamientoDePrueba {

    static final LocalDate HOY = LocalDate.now();

    private AlojamientoDePrueba() {
    }

    static Alojamiento crear() {
        Alojamiento alojamiento = new Alojamiento("Aparta-Suites UQ", "Salento");
        alojamiento.registrarApartamento(apartamento("APT-101", 2));
        alojamiento.registrarApartamento(apartamento("APT-201", 6));
        alojamiento.registrarHuesped(new Huesped("Laura", LocalDate.of(1995, 7, 18), "1094", "320", "l@x.co",
                "laura", "laura123"));
        alojamiento.contratarEmpleado(new Administrador("Diana", LocalDate.of(1985, 4, 12), "1", "310", "d@x.co",
                "admin", "admin123", HOY));
        alojamiento.contratarEmpleado(new Recepcionista("Julián", LocalDate.of(1998, 9, 30), "2", "311", "j@x.co",
                "recepcion", "recepcion123", HOY));
        return alojamiento;
    }

    /** Tarifa Baja 100.000 por persona y noche en todas las temporadas, para cuentas fáciles. */
    private static Apartamento apartamento(String codigo, int capacidad) {
        Apartamento apartamento = new Apartamento(codigo, "Apto " + codigo, 2, capacidad, new ArrayList<>(),
                EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
        for (TipoTemporada temporada : TipoTemporada.values()) {
            apartamento.definirTarifa(temporada, 100_000);
        }
        return apartamento;
    }

    static List<Ocupante> adultos(int cantidad) {
        List<Ocupante> lista = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            lista.add(new Ocupante("Adulto " + i, LocalDate.of(1990, 1, 1)));
        }
        return lista;
    }
}
