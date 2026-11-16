package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.Tarifa;

import java.util.List;

public record ApartamentoResponse(String codigo, String nombre, int numDormitorios, int capacidadMaxima,
                                  List<String> caracteristicas, EstadoApartamento estado,
                                  String motivoFueraServicio, boolean activo, List<TarifaDto> tarifas,
                                  List<String> fotos) {

    public static ApartamentoResponse desde(Apartamento a) {
        List<TarifaDto> tarifas = a.getTarifas().stream()
                .map((Tarifa t) -> new TarifaDto(t.getTipoTemporada(), t.getValorPersonaNoche()))
                .toList();
        return new ApartamentoResponse(a.getCodigo(), a.getNombre(), a.getNumDormitorios(), a.getCapacidadMaxima(),
                a.getCaracteristicas(), a.getEstado(), a.getMotivoFueraServicio(), a.isActivo(), tarifas,
                a.getFotos());
    }
}
