package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.persona.Huesped;

/** Puerto de entrada: perfil del huésped y registro de huéspedes en recepción. */
public interface GestionHuespedUseCase {

    Huesped buscar(String documento);

    /** Recepción registra a un huésped que llega o llama: usuario y contraseña inicial = documento. */
    Huesped registrarEnRecepcion(Huesped huesped);

    Huesped actualizarPerfil(String documento, String telefono, String correo, String foto);
}
