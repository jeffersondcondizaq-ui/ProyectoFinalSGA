package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.GestionHuespedUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.persona.Huesped;

public class HuespedService extends ServicioBase implements GestionHuespedUseCase {

    public HuespedService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public Huesped buscar(String documento) {
        return ejecutar(alojamiento -> alojamiento.buscarHuesped(documento));
    }

    @Override
    public Huesped registrarEnRecepcion(Huesped huesped) {
        return ejecutar(alojamiento -> {
            alojamiento.registrarHuesped(huesped);
            return huesped;
        });
    }

    @Override
    public Huesped actualizarPerfil(String documento, String telefono, String correo, String foto) {
        return ejecutar(alojamiento -> {
            Huesped huesped = alojamiento.buscarHuesped(documento);
            if (telefono == null || telefono.isBlank() || correo == null || !correo.contains("@")) {
                throw new ReglaNegocioException("Teléfono y correo válidos son obligatorios");
            }
            huesped.actualizarPerfil(telefono, correo);
            if (foto != null) {
                huesped.setFoto(foto);
            }
            return huesped;
        });
    }
}
