package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.folio.Folio;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.TipoCargo;

/** Puerto de entrada: folio de una reserva (pagos, cargos y correcciones). */
public interface FolioUseCase {

    Folio obtener(String codigoReserva);

    Folio registrarPago(String codigoReserva, MedioPago medio, long valor, String descripcion);

    Folio registrarCargo(String codigoReserva, TipoCargo tipo, long valor, String descripcion);

    /** Corrige el movimiento en la posición indicada con un movimiento contrario. */
    Folio corregir(String codigoReserva, int indiceMovimiento);
}
