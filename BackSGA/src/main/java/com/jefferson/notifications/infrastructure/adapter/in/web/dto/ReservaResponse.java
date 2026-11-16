package com.jefferson.notifications.infrastructure.adapter.in.web.dto;

import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import com.jefferson.notifications.domain.model.reserva.ReservaPortal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(String codigo, Canal canal, EstadoReserva estado, String documentoTitular,
                              String nombreTitular, String codigoApartamento, LocalDate fechaEntrada,
                              LocalDate fechaSalida, LocalDateTime fechaCreacion, List<OcupanteDto> ocupantes,
                              long valorEstancia, Long anticipo, long saldo, String codigoExterno,
                              String plataforma) {

    public static ReservaResponse desde(Reserva r) {
        Long anticipo = r instanceof ReservaPortal portal ? portal.calcularAnticipo() : null;
        String codigoExterno = r instanceof ReservaExterna externa ? externa.getCodigoExterno() : null;
        String plataforma = r instanceof ReservaExterna externa ? externa.getPlataforma() : null;
        return new ReservaResponse(r.getCodigo(), r.getCanal(), r.getEstado(), r.getTitular().getDocumento(),
                r.getTitular().getNombre(), r.getApartamento().getCodigo(), r.getFechaEntrada(), r.getFechaSalida(),
                r.getFechaCreacion(), r.getOcupantes().stream().map(OcupanteDto::desde).toList(),
                r.getValorEstancia(), anticipo, r.getFolio().calcularSaldo(), codigoExterno, plataforma);
    }

    public static List<ReservaResponse> desde(List<Reserva> reservas) {
        return reservas.stream().map(ReservaResponse::desde).toList();
    }
}
