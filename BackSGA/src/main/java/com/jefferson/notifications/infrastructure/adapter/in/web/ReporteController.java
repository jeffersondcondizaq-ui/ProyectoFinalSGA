package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.ReporteUseCase;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.reserva.Canal;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.ReservaResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** API de reportes (ReporteApi en el front). Solo el administrador. Rangos con "hasta" incluido. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteUseCase reportes;
    private final SesionWeb sesion;

    public ReporteController(ReporteUseCase reportes, SesionWeb sesion) {
        this.reportes = reportes;
        this.sesion = sesion;
    }

    public record OcupacionResponse(String codigoApartamento, String nombre, int nochesOcupadas, double porcentaje) {
    }

    public record IngresosResponse(LocalDate desde, LocalDate hasta, long total) {
    }

    public record NovedadesResponse(String codigoApartamento, String nombre, int cantidad) {
    }

    @GetMapping("/ocupacion")
    public List<OcupacionResponse> ocupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        LocalDate hastaExclusivo = hasta.plusDays(1);
        long nochesDelRango = ChronoUnit.DAYS.between(desde, hastaExclusivo);
        List<OcupacionResponse> respuesta = new ArrayList<>();
        for (Map.Entry<Apartamento, Integer> fila : reportes.ocupacion(desde, hastaExclusivo).entrySet()) {
            double porcentaje = nochesDelRango > 0 ? Math.round(fila.getValue() * 1000.0 / nochesDelRango) / 10.0 : 0;
            respuesta.add(new OcupacionResponse(fila.getKey().getCodigo(), fila.getKey().getNombre(), fila.getValue(),
                    porcentaje));
        }
        return respuesta;
    }

    @GetMapping("/ingresos")
    public IngresosResponse ingresos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return new IngresosResponse(desde, hasta, reportes.ingresos(desde, hasta));
    }

    @GetMapping("/reservas-por-canal")
    public Map<Canal, Integer> reservasPorCanal() {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return reportes.reservasPorCanal();
    }

    @GetMapping("/saldos-pendientes")
    public List<ReservaResponse> saldosPendientes() {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return ReservaResponse.desde(reportes.saldosPendientes());
    }

    @GetMapping("/novedades")
    public List<NovedadesResponse> novedades() {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return reportes.apartamentosConMasNovedades().stream()
                .map(a -> new NovedadesResponse(a.getCodigo(), a.getNombre(), a.getNovedades().size()))
                .toList();
    }
}
