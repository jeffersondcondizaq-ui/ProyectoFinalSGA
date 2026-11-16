package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;

public record TarifaDto(TipoTemporada tipoTemporada, long valorPersonaNoche) {
}
