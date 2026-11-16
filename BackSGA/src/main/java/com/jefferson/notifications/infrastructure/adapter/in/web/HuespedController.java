package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.GestionHuespedUseCase;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.HuespedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** API de huéspedes (HuespedApi en el front): perfil propio y registro en recepción. */
@RestController
@RequestMapping("/api/huespedes")
public class HuespedController {

    private final GestionHuespedUseCase huespedes;
    private final SesionWeb sesion;

    public HuespedController(GestionHuespedUseCase huespedes, SesionWeb sesion) {
        this.huespedes = huespedes;
        this.sesion = sesion;
    }

    public record NuevoHuespedRequest(
            @NotBlank(message = "El documento es obligatorio") String documento,
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento,
            @NotBlank(message = "El teléfono es obligatorio") String telefono,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo) {
    }

    public record PerfilRequest(String telefono, String correo, String foto) {
    }

    @GetMapping("/yo")
    public HuespedResponse miPerfil() {
        Usuario usuario = sesion.exigirRol(Rol.HUESPED);
        return HuespedResponse.desde(huespedes.buscar(usuario.getDocumento()));
    }

    @PutMapping("/yo")
    public HuespedResponse actualizarPerfil(@RequestBody PerfilRequest r) {
        Usuario usuario = sesion.exigirRol(Rol.HUESPED);
        return HuespedResponse.desde(huespedes.actualizarPerfil(usuario.getDocumento(), r.telefono(), r.correo(),
                r.foto()));
    }

    @GetMapping("/{documento}")
    public HuespedResponse buscar(@PathVariable String documento) {
        sesion.exigirEmpleado();
        return HuespedResponse.desde(huespedes.buscar(documento));
    }

    /** Recepción registra al huésped: su usuario y su contraseña inicial son el documento. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HuespedResponse registrar(@Valid @RequestBody NuevoHuespedRequest r) {
        sesion.exigirEmpleado();
        Huesped huesped = new Huesped(r.nombre(), r.fechaNacimiento(), r.documento(), r.telefono(), r.correo(),
                r.documento(), r.documento());
        return HuespedResponse.desde(huespedes.registrarEnRecepcion(huesped));
    }
}
