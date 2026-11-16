package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.persona.Empleado;

import java.util.List;

/** Puerto de entrada: el administrador contrata, edita y desvincula empleados. */
public interface GestionEmpleadoUseCase {

    List<Empleado> listar();

    Empleado contratar(Empleado empleado);

    Empleado actualizar(String documento, String telefono, String correo);

    void desvincular(String documento);
}
