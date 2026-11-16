package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.persona.Ocupante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

/** Ocupante: solo nombre y fecha de nacimiento; la edad la calcula el back. */
public record OcupanteDto(
        @NotBlank(message = "El nombre de cada ocupante es obligatorio") String nombre,
        @NotNull(message = "La fecha de nacimiento de cada ocupante es obligatoria")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura") LocalDate fechaNacimiento) {

    public Ocupante aDominio() {
        return new Ocupante(nombre, fechaNacimiento);
    }

    public static OcupanteDto desde(Ocupante o) {
        return new OcupanteDto(o.getNombre(), o.getFechaNacimiento());
    }

    public static List<Ocupante> aDominio(List<OcupanteDto> lista) {
        return lista.stream().map(OcupanteDto::aDominio).toList();
    }
}
