package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.persona.Rol;

public record SesionResponse(String token, String usuario, String nombre, Rol rol) {
}