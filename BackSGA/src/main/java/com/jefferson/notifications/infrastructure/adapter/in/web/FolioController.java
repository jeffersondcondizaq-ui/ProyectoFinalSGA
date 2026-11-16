package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.FolioUseCase;
import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.domain.model.folio.Cargo;
import com.jefferson.notifications.domain.model.folio.Folio;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.MovimientoFolio;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.folio.TipoCargo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** API del folio de una reserva (FolioApi en el front). El id de cada movimiento es su posición. */
@RestController
@RequestMapping("/api/reservas/{codigo}/folio")
public class FolioController {

    private final FolioUseCase folios;
    private final ReservaUseCase reservas;
    private final SesionWeb sesion;

    public FolioController(FolioUseCase folios, ReservaUseCase reservas, SesionWeb sesion) {
        this.folios = folios;
        this.reservas = reservas;
        this.sesion = sesion;
    }

    public record PagoRequest(@NotNull(message = "El medio de pago es obligatorio") MedioPago medio,
                              @Positive(message = "El valor debe ser mayor que cero") long valor,
                              String descripcion) {
    }

    public record CargoRequest(@NotNull(message = "El tipo de cargo es obligatorio") TipoCargo tipo,
                               @Positive(message = "El valor debe ser mayor que cero") long valor,
                               String descripcion) {
    }

    public record MovimientoResponse(String id, String tipo, LocalDateTime fecha, long valor, String descripcion,
                                     boolean esCorreccion, TipoCargo tipoCargo, MedioPago medio) {
    }

    public record FolioResponse(String codigoReserva, List<MovimientoResponse> movimientos, long totalCargos,
                                long totalPagos, long saldo) {
    }

    /** El huésped ve el folio de sus reservas; recepción, el de todas. */
    @GetMapping
    public FolioResponse obtener(@PathVariable String codigo) {
        sesion.exigirAccesoA(reservas.buscar(codigo));
        return aRespuesta(codigo, folios.obtener(codigo));
    }

    /** El huésped puede pagar el anticipo de su reserva por el portal; recepción registra cualquier pago. */
    @PostMapping("/pagos")
    public FolioResponse registrarPago(@PathVariable String codigo, @Valid @RequestBody PagoRequest r) {
        sesion.exigirAccesoA(reservas.buscar(codigo));
        return aRespuesta(codigo, folios.registrarPago(codigo, r.medio(), r.valor(), r.descripcion()));
    }

    @PostMapping("/cargos")
    public FolioResponse registrarCargo(@PathVariable String codigo, @Valid @RequestBody CargoRequest r) {
        sesion.exigirEmpleado();
        return aRespuesta(codigo, folios.registrarCargo(codigo, r.tipo(), r.valor(), r.descripcion()));
    }

    @PostMapping("/movimientos/{id}/correccion")
    public FolioResponse corregir(@PathVariable String codigo, @PathVariable int id) {
        sesion.exigirEmpleado();
        return aRespuesta(codigo, folios.corregir(codigo, id));
    }

    private FolioResponse aRespuesta(String codigo, Folio folio) {
        List<MovimientoResponse> movimientos = new ArrayList<>();
        List<MovimientoFolio> lista = folio.getMovimientos();
        for (int i = 0; i < lista.size(); i++) {
            MovimientoFolio m = lista.get(i);
            String tipo = m instanceof Cargo ? "CARGO" : m instanceof Pago ? "PAGO" : "DEVOLUCION";
            TipoCargo tipoCargo = m instanceof Cargo cargo ? cargo.getTipo() : null;
            MedioPago medio = m instanceof Pago pago ? pago.getMedio() : null;
            movimientos.add(new MovimientoResponse(String.valueOf(i), tipo, m.getFecha(), m.getValor(),
                    m.getDescripcion(), m.isEsCorreccion(), tipoCargo, medio));
        }
        return new FolioResponse(codigo, movimientos, folio.calcularTotalCargos(), folio.calcularTotalPagos(),
                folio.calcularSaldo());
    }
}
