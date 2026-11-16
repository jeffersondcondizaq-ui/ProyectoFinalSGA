package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.GestionEmpleadoUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.persona.Empleado;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoService extends ServicioBase implements GestionEmpleadoUseCase {

    public EmpleadoService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public List<Empleado> listar() {
        return ejecutar(alojamiento -> new ArrayList<>(alojamiento.getEmpleados()));
    }

    @Override
    public Empleado contratar(Empleado empleado) {
        return ejecutar(alojamiento -> {
            alojamiento.contratarEmpleado(empleado);
            return empleado;
        });
    }

    @Override
    public Empleado actualizar(String documento, String telefono, String correo) {
        return ejecutar(alojamiento -> {
            alojamiento.actualizarEmpleado(documento, telefono, correo);
            return alojamiento.buscarEmpleado(documento);
        });
    }

    @Override
    public void desvincular(String documento) {
        ejecutar(alojamiento -> alojamiento.desvincularEmpleado(documento));
    }
}
