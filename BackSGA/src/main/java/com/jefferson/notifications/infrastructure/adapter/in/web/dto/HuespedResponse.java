package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.persona.Huesped;

import java.time.LocalDate;

public record HuespedResponse(String documento, String nombre, LocalDate fechaNacimiento, int edad,
                              String telefono, String correo, String usuario, String foto) {

    public static HuespedResponse desde(Huesped huesped) {
        return new HuespedResponse(huesped.getDocumento(), huesped.getNombre(), huesped.getFechaNacimiento(),
                huesped.calcularEdad(LocalDate.now()), huesped.getTelefono(), huesped.getCorreo(),
                huesped.getUsuario(), huesped.getFoto());
    }
}