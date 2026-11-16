package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.GestionApartamentoUseCase;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoNovedad;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.ApartamentoResponse;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.TarifaDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** API de apartamentos (ApartamentoApi en el front). Consultar es público; administrar es del admin. */
@RestController
@RequestMapping("/api/apartamentos")
public class ApartamentoController {

    private final GestionApartamentoUseCase apartamentos;
    private final SesionWeb sesion;

    public ApartamentoController(GestionApartamentoUseCase apartamentos, SesionWeb sesion) {
        this.apartamentos = apartamentos;
        this.sesion = sesion;
    }

    public record CrearApartamentoRequest(
            @NotBlank(message = "El código es obligatorio") String codigo,
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            int numDormitorios,
            int capacidadMaxima,
            List<String> caracteristicas,
            List<TarifaDto> tarifas,
            List<String> fotos) {
    }

    public record ActualizarApartamentoRequest(String nombre, Integer numDormitorios, Integer capacidadMaxima,
                                               List<String> caracteristicas) {
    }

    public record BloqueoRequest(@NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
                                 @NotNull(message = "La fecha de fin es obligatoria") LocalDate fechaFin,
                                 String motivo) {
    }

    public record EstadoRequest(@NotNull(message = "El estado es obligatorio") EstadoApartamento estado,
                                String motivo) {
    }

    public record NovedadRequest(@NotNull(message = "El tipo es obligatorio") TipoNovedad tipo,
                                 String descripcion) {
    }

    /** Público: el catálogo ve los activos; el administrador ve también los eliminados. */
    @GetMapping
    public List<ApartamentoResponse> listar() {
        Usuario usuario = sesion.usuarioOpcional();
        boolean esAdmin = usuario != null && usuario.getRol() == Rol.ADMINISTRADOR;
        return apartamentos.listar(esAdmin).stream().map(ApartamentoResponse::desde).toList();
    }

    @GetMapping("/{codigo}")
    public ApartamentoResponse buscar(@PathVariable String codigo) {
        return ApartamentoResponse.desde(apartamentos.buscar(codigo));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApartamentoResponse crear(@Valid @RequestBody CrearApartamentoRequest r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        Apartamento apartamento = new Apartamento(r.codigo(), r.nombre(), r.numDormitorios(), r.capacidadMaxima(),
                r.caracteristicas() == null ? new ArrayList<>() : new ArrayList<>(r.caracteristicas()),
                EstadoApartamento.PREPARADO, null, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                r.fotos() == null ? new ArrayList<>() : new ArrayList<>(r.fotos()));
        apartamentos.registrar(apartamento);
        if (r.tarifas() != null && !r.tarifas().isEmpty()) {
            apartamentos.definirTarifas(r.codigo(), aMapa(r.tarifas()));
        }
        return ApartamentoResponse.desde(apartamento);
    }

    @PutMapping("/{codigo}")
    public ApartamentoResponse actualizar(@PathVariable String codigo, @RequestBody ActualizarApartamentoRequest r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return ApartamentoResponse.desde(apartamentos.actualizar(codigo, r.nombre(), r.numDormitorios(),
                r.capacidadMaxima(), r.caracteristicas()));
    }

    /** Eliminación lógica: el apartamento queda inactivo. */
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        apartamentos.eliminar(codigo);
    }

    @PutMapping("/{codigo}/tarifas")
    public ApartamentoResponse definirTarifas(@PathVariable String codigo, @RequestBody List<TarifaDto> tarifas) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return ApartamentoResponse.desde(apartamentos.definirTarifas(codigo, aMapa(tarifas)));
    }

    @PostMapping("/{codigo}/bloqueos")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarBloqueo(@PathVariable String codigo, @Valid @RequestBody BloqueoRequest r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        apartamentos.registrarBloqueo(codigo, new Bloqueo(r.fechaInicio(), r.fechaFin(), r.motivo()));
    }

    /** Recepción cambia el estado operativo (por ejemplo, de En preparación a Preparado). */
    @PatchMapping("/{codigo}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable String codigo, @Valid @RequestBody EstadoRequest r) {
        sesion.exigirEmpleado();
        apartamentos.cambiarEstado(codigo, r.estado(), r.motivo());
    }

    @PostMapping("/{codigo}/novedades")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarNovedad(@PathVariable String codigo, @Valid @RequestBody NovedadRequest r) {
        Usuario quien = sesion.exigirEmpleado();
        apartamentos.registrarNovedad(codigo, r.tipo(), r.descripcion(), quien);
    }

    private Map<TipoTemporada, Long> aMapa(List<TarifaDto> tarifas) {
        Map<TipoTemporada, Long> mapa = new EnumMap<>(TipoTemporada.class);
        for (TarifaDto tarifa : tarifas) {
            mapa.put(tarifa.tipoTemporada(), tarifa.valorPersonaNoche());
        }
        return mapa;
    }
}
