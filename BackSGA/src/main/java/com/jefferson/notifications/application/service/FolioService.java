package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.FolioUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.RecursoNoEncontradoException;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.folio.Cargo;
import com.jefferson.notifications.domain.model.folio.Folio;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.folio.TipoCargo;
import com.jefferson.notifications.domain.model.reserva.EstadoReserva;
import com.jefferson.notifications.domain.model.reserva.Reserva;

public class FolioService extends ServicioBase implements FolioUseCase {

    public FolioService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public Folio obtener(String codigoReserva) {
        return ejecutar(alojamiento -> alojamiento.buscarReserva(codigoReserva).getFolio());
    }

    @Override
    public Folio registrarPago(String codigoReserva, MedioPago medio, long valor, String descripcion) {
        return ejecutar(alojamiento -> {
            Reserva reserva = alojamiento.buscarReserva(codigoReserva);
            validarQueAdmiteMovimientos(reserva);
            if (medio == null || !reserva.getFolio().registrarPago(new Pago(medio, valor, descripcion, false))) {
                throw new ReglaNegocioException("El pago debe tener medio y un valor mayor que cero");
            }
            return reserva.getFolio();
        });
    }

    @Override
    public Folio registrarCargo(String codigoReserva, TipoCargo tipo, long valor, String descripcion) {
        return ejecutar(alojamiento -> {
            Reserva reserva = alojamiento.buscarReserva(codigoReserva);
            validarQueAdmiteMovimientos(reserva);
            if (tipo == TipoCargo.ALOJAMIENTO) {
                throw new ReglaNegocioException("El cargo de alojamiento lo registra el sistema al crear la reserva");
            }
            if (tipo == null || !reserva.getFolio().agregarCargo(new Cargo(tipo, valor, descripcion, false))) {
                throw new ReglaNegocioException("El cargo debe tener tipo y un valor mayor que cero");
            }
            return reserva.getFolio();
        });
    }

    @Override
    public Folio corregir(String codigoReserva, int indiceMovimiento) {
        return ejecutar(alojamiento -> {
            Folio folio = alojamiento.buscarReserva(codigoReserva).getFolio();
            if (indiceMovimiento < 0 || indiceMovimiento >= folio.getMovimientos().size()) {
                throw new RecursoNoEncontradoException("No existe el movimiento " + indiceMovimiento);
            }
            if (!folio.corregir(folio.getMovimientos().get(indiceMovimiento))) {
                throw new ReglaNegocioException("Una corrección no se puede corregir de nuevo");
            }
            return folio;
        });
    }

    private void validarQueAdmiteMovimientos(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.NO_SHOW) {
            throw new ReglaNegocioException("La reserva " + reserva.getCodigo() + " está " + reserva.getEstado()
                    + ": su folio ya fue liquidado");
        }
    }
}
