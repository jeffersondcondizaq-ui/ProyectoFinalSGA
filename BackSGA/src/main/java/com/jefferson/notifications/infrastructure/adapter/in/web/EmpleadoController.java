package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.GestionEmpleadoUseCase;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.persona.Administrador;
import com.jefferson.notifications.domain.model.persona.Empleado;
import com.jefferson.notifications.domain.model.persona.Recepcionista;
import com.jefferson.notifications.domain.model.persona.Rol;
import com.jefferson.notifications.domain.model.persona.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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
import java.util.List;

/** API de empleados (EmpleadoApi en el front). Solo el administrador. */
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final GestionEmpleadoUseCase empleados;
    private final SesionWeb sesion;

    public EmpleadoController(GestionEmpleadoUseCase empleados, SesionWeb sesion) {
        this.empleados = empleados;
        this.sesion = sesion;
    }

    public record ContratarRequest(
            @NotBlank(message = "El documento es obligatorio") String documento,
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento,
            @NotBlank(message = "El teléfono es obligatorio") String telefono,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo,
            @NotBlank(message = "El usuario es obligatorio") String usuario,
            @NotNull(message = "El rol es obligatorio") Rol rol,
            LocalDate fechaContratacion,
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String contrasena) {
    }

    public record ActualizarEmpleadoRequest(String telefono, String correo) {
    }

    public record EmpleadoResponse(String documento, String nombre, LocalDate fechaNacimiento, String telefono,
                                   String correo, String usuario, Rol rol, LocalDate fechaContratacion,
                                   boolean activo) {

        static EmpleadoResponse desde(Empleado e) {
            return new EmpleadoResponse(e.getDocumento(), e.getNombre(), e.getFechaNacimiento(), e.getTelefono(),
                    e.getCorreo(), e.getUsuario(), e.getRol(), e.getFechaContratacion(), e.isActivo());
        }
    }

    @GetMapping
    public List<EmpleadoResponse> listar() {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return empleados.listar().stream().map(EmpleadoResponse::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponse contratar(@Valid @RequestBody ContratarRequest r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        LocalDate contratacion = r.fechaContratacion() != null ? r.fechaContratacion() : LocalDate.now();
        Empleado empleado = switch (r.rol()) {
            case ADMINISTRADOR -> new Administrador(r.nombre(), r.fechaNacimiento(), r.documento(), r.telefono(),
                    r.correo(), r.usuario(), r.contrasena(), contratacion);
            case RECEPCIONISTA -> new Recepcionista(r.nombre(), r.fechaNacimiento(), r.documento(), r.telefono(),
                    r.correo(), r.usuario(), r.contrasena(), contratacion);
            case HUESPED -> throw new ReglaNegocioException("Un empleado no puede tener el rol de huésped");
        };
        return EmpleadoResponse.desde(empleados.contratar(empleado));
    }

    @PutMapping("/{documento}")
    public EmpleadoResponse actualizar(@PathVariable String documento, @RequestBody ActualizarEmpleadoRequest r) {
        sesion.exigirRol(Rol.ADMINISTRADOR);
        return EmpleadoResponse.desde(empleados.actualizar(documento, r.telefono(), r.correo()));
    }

    @PatchMapping("/{documento}/desvincular")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(@PathVariable String documento) {
        Usuario admin = sesion.exigirRol(Rol.ADMINISTRADOR);
        if (admin.getDocumento().equals(documento)) {
            throw new ReglaNegocioException("No puedes desvincularte a ti mismo");
        }
        empleados.desvincular(documento);
    }
}
