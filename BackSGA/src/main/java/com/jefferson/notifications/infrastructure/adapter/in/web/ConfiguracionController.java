package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.ConfiguracionUseCase;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** API de temporadas y política de cancelación (ConfiguracionApi en el front). */
@RestController
@RequestMapping("/api")
public class ConfiguracionController {

    private final ConfiguracionUseCase configuracion;
    private final SesionWeb sesion;

    public ConfiguracionController(ConfiguracionUseCase configuracion, SesionWeb sesion) {
        this.configuracion = configuracion;
        this.sesion = sesion;
    }

    public record TemporadaDto(@NotNull(message = "El tipo es obligatorio") TipoTemporada tipo,
                               @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
                               @NotNull(message = "La fecha de fin es obligatoria") LocalDate fechaFin,
                               String descripcion) {

        static TemporadaDto desde(PeriodoTemporada p) {
            return new TemporadaDto(p.getTipo(), p.getFechaInicio(), p.getFechaFin(), p.getDescripcion());
        }
    }

    public record PoliticaDto(int diasDevolucionTotal, int horasRetencionTotal, int porcentajeRetencionParcial) {

        static PoliticaDto desde(PoliticaCancelacion p) {
            return new PoliticaDto(p.getDiasDevolucionTotal(), p.getHorasRetencionTotal(),
                    p.getPorcentajeRetencionParcial());
        }
    }

    @GetMapping("/temporadas")
    public List<TemporadaDto> listarTemporadas() {
        return configuracion.listarTemporadas().stream().map(TemporadaDto::desde).toList();
    }

    @PostMapping("/temporadas")
    @ResponseStatus(HttpStatus.CREATED)
    public TemporadaDto registrarTemporada(@Valid @RequestBody TemporadaDto r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return TemporadaDto.desde(configuracion.registrarTemporada(
                new PeriodoTemporada(r.tipo(), r.fechaInicio(), r.fechaFin(), r.descripcion())));
    }

    @GetMapping("/politica-cancelacion")
    public PoliticaDto obtenerPolitica() {
        return PoliticaDto.desde(configuracion.obtenerPolitica());
    }

    /** Solo afecta a las reservas nuevas: las existentes tienen su política congelada. */
    @PutMapping("/politica-cancelacion")
    public PoliticaDto configurarPolitica(@RequestBody PoliticaDto r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return PoliticaDto.desde(configuracion.configurarPolitica(new PoliticaCancelacion(
                r.diasDevolucionTotal(), r.horasRetencionTotal(), r.porcentajeRetencionParcial())));
    }
}
