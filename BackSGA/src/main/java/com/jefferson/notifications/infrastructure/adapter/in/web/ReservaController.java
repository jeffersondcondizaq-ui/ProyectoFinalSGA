package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.application.port.in.ReservaUseCase.ResultadoCancelacion;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.domain.model.reserva.DetalleNoche;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.ApartamentoResponse;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.OcupanteDto;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.ReservaResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** API de reservas (ReservaApi en el front): disponibilidad, cotización, canales y ciclo de vida. */
@RestController
@RequestMapping("/api")
public class ReservaController {

    private final ReservaUseCase reservas;
    private final SesionWeb sesion;

    public ReservaController(ReservaUseCase reservas, SesionWeb sesion) {
        this.reservas = reservas;
        this.sesion = sesion;
    }

    public record CotizarRequest(
            @NotBlank(message = "El apartamento es obligatorio") String codigoApartamento,
            @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,
            @NotEmpty(message = "Debe haber al menos un ocupante") List<@Valid OcupanteDto> ocupantes) {
    }

    public record CrearReservaRequest(
            @NotBlank(message = "El apartamento es obligatorio") String codigoApartamento,
            @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,
            @NotEmpty(message = "Debe haber al menos un ocupante") List<@Valid OcupanteDto> ocupantes,
            String documentoTitular) {
    }

    public record ReservaExternaRequest(
            @NotBlank(message = "El apartamento es obligatorio") String codigoApartamento,
            @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,
            @NotEmpty(message = "Debe haber al menos un ocupante") List<@Valid OcupanteDto> ocupantes,
            @NotBlank(message = "El documento del titular es obligatorio") String documentoTitular,
            @NotBlank(message = "El código externo es obligatorio") String codigoExterno,
            @NotBlank(message = "La plataforma es obligatoria") String plataforma) {
    }

    public record DetalleNocheResponse(LocalDate fecha, TipoTemporada temporada, long tarifaPersona, int pagadores,
                                       long subtotal) {
    }

    public record CotizacionResponse(String codigoApartamento, LocalDate fechaEntrada, LocalDate fechaSalida,
                                     List<DetalleNocheResponse> detalleNoches, long valorTotal, long anticipo) {
    }

    public record CancelacionResponse(long totalPagado, long retencion, long devolucion, String regla) {
    }

    public record ConflictoResponse(String codigoExterno, LocalDateTime fechaRegistro, String codigoReservaExistente,
                                    String descripcion) {
    }

    // ---------------- Públicos ----------------

    @GetMapping("/disponibilidad")
    public List<ApartamentoResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam int personas) {
        return reservas.consultarDisponibles(entrada, salida, personas).stream()
                .map(ApartamentoResponse::desde).toList();
    }

    @PostMapping("/cotizaciones")
    public CotizacionResponse cotizar(@Valid @RequestBody CotizarRequest r) {
        List<DetalleNoche> detalle = reservas.cotizar(r.codigoApartamento(), r.fechaEntrada(), r.fechaSalida(),
                OcupanteDto.aDominio(r.ocupantes()));
        long total = 0;
        for (DetalleNoche noche : detalle) {
            total += noche.calcularSubtotal();
        }
        List<DetalleNocheResponse> noches = detalle.stream()
                .map(n -> new DetalleNocheResponse(n.getFecha(), n.getTemporada(), n.getTarifaPersona(),
                        n.getPagadores(), n.calcularSubtotal()))
                .toList();
        return new CotizacionResponse(r.codigoApartamento(), r.fechaEntrada(), r.fechaSalida(), noches, total,
                total * 30 / 100);
    }

    // ---------------- Creación por canal ----------------

    /** Portal: el titular es el huésped que tiene la sesión. */
    @PostMapping("/reservas/portal")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crearPortal(@Valid @RequestBody CrearReservaRequest r) {
        Usuario huesped = sesion.exigirRol(Rol.HUESPED);
        return ReservaResponse.desde(reservas.crearPortal(huesped.getDocumento(), r.codigoApartamento(),
                r.fechaEntrada(), r.fechaSalida(), OcupanteDto.aDominio(r.ocupantes())));
    }

    /** Directa: recepción la registra para un huésped que llama o llega. */
    @PostMapping("/reservas/directa")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crearDirecta(@Valid @RequestBody CrearReservaRequest r) {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.crearDirecta(r.documentoTitular(), r.codigoApartamento(),
                r.fechaEntrada(), r.fechaSalida(), OcupanteDto.aDominio(r.ocupantes())));
    }

    /** Externa: simula la plataforma de terceros. Si choca, responde 409 y queda el conflicto. */
    @PostMapping("/reservas/externa")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse recibirExterna(@Valid @RequestBody ReservaExternaRequest r) {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.recibirExterna(r.documentoTitular(), r.codigoApartamento(),
                r.fechaEntrada(), r.fechaSalida(), OcupanteDto.aDominio(r.ocupantes()), r.codigoExterno(),
                r.plataforma()));
    }

    // ---------------- Consultas ----------------

    @GetMapping("/reservas/mias")
    public List<ReservaResponse> misReservas() {
        Usuario huesped = sesion.exigirRol(Rol.HUESPED);
        return ReservaResponse.desde(reservas.reservasDelHuesped(huesped.getDocumento()));
    }

    @GetMapping("/reservas")
    public List<ReservaResponse> listar() {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.listar());
    }

    @GetMapping("/reservas/{codigo}")
    public ReservaResponse buscar(@PathVariable String codigo) {
        return ReservaResponse.desde(reservaPropia(codigo));
    }

    @GetMapping("/conflictos")
    public List<ConflictoResponse> conflictos() {
        sesion.exigirEmpleado();
        return reservas.conflictos().stream()
                .map(c -> new ConflictoResponse(c.getCodigoExterno(), c.getFechaRegistro(),
                        c.getReservaExistente() == null ? null : c.getReservaExistente().getCodigo(),
                        c.getDescripcion()))
                .toList();
    }

    // ---------------- Ciclo de vida ----------------

    @PostMapping("/reservas/{codigo}/confirmar")
    public ReservaResponse confirmar(@PathVariable String codigo) {
        reservaPropia(codigo);
        return ReservaResponse.desde(reservas.confirmar(codigo));
    }

    @GetMapping("/reservas/{codigo}/devolucion")
    public CancelacionResponse simularCancelacion(@PathVariable String codigo) {
        reservaPropia(codigo);
        return aRespuesta(reservas.simularCancelacion(codigo));
    }

    @PostMapping("/reservas/{codigo}/cancelar")
    public CancelacionResponse cancelar(@PathVariable String codigo) {
        reservaPropia(codigo);
        return aRespuesta(reservas.cancelar(codigo));
    }

    @PostMapping("/reservas/{codigo}/check-in")
    public ReservaResponse checkIn(@PathVariable String codigo) {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.hacerCheckIn(codigo));
    }

    @PostMapping("/reservas/{codigo}/check-out")
    public ReservaResponse checkOut(@PathVariable String codigo) {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.hacerCheckOut(codigo));
    }

    @PostMapping("/reservas/{codigo}/no-show")
    public ReservaResponse noShow(@PathVariable String codigo) {
        sesion.exigirEmpleado();
        return ReservaResponse.desde(reservas.declararNoShow(codigo));
    }

    /** Busca la reserva y verifica que quien pregunta pueda verla (el huésped, solo las suyas). */
    private Reserva reservaPropia(String codigo) {
        Reserva reserva = reservas.buscar(codigo);
        sesion.exigirAccesoA(reserva);
        return reserva;
    }

    private CancelacionResponse aRespuesta(ResultadoCancelacion r) {
        return new CancelacionResponse(r.totalPagado(), r.retencion(), r.devolucion(), r.regla());
    }
}
