package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Registro público de un huésped (mismos campos y validaciones que el formulario del front). */
public record RegistroHuespedRequest(
        @NotBlank(message = "El documento es obligatorio") String documento,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento,
        @NotBlank(message = "El teléfono es obligatorio") String telefono,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido") String correo,
        @NotBlank(message = "El usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._]{4,20}$",
                message = "El usuario debe tener de 4 a 20 letras, números, puntos o guiones bajos") String usuario,
        String foto,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String contrasena) {
}