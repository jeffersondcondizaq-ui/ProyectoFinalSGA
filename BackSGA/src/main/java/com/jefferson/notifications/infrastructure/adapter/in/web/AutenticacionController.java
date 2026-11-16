package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.port.in.AutenticacionUseCase;
import com.jefferson.notifications.application.port.in.AutenticacionUseCase.SesionIniciada;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.CambioContrasenaRequest;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.HuespedResponse;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.LoginRequest;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.RegistroHuespedRequest;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.SesionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionUseCase autenticacion;

    public AutenticacionController(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    /** POST /api/auth/login → 200 { token, usuario, nombre, rol } o 401 */
    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest request) {
        SesionIniciada sesion = autenticacion.iniciarSesion(request.usuario(), request.contrasena());
        Usuario usuario = sesion.usuario();
        return new SesionResponse(sesion.token(), usuario.getUsuario(), usuario.getNombre(), usuario.getRol());
    }

    /** POST /api/auth/registro → 201 con el huésped creado (sin contraseña) o 409 si ya existe */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public HuespedResponse registrar(@Valid @RequestBody RegistroHuespedRequest request) {
        Huesped huesped = new Huesped(request.nombre(), request.fechaNacimiento(), request.documento(),
                request.telefono(), request.correo(), request.usuario(), request.contrasena());
        huesped.setFoto(request.foto());
        return HuespedResponse.desde(autenticacion.registrarHuesped(huesped));
    }

    /** PUT /api/auth/contrasena (con sesión) → 204 */
    @PutMapping("/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String cabecera,
                                  @Valid @RequestBody CambioContrasenaRequest request) {
        autenticacion.cambiarContrasena(CabeceraAutorizacion.token(cabecera), request.actual(), request.nueva());
    }
}