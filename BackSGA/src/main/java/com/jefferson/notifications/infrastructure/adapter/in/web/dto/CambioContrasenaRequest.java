package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria") String actual,
        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = 6, message = "La contraseña nueva debe tener al menos 6 caracteres") String nueva) {
}