package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.ReservaUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.ConflictoCanal;
import com.jefferson.notifications.domain.model.reserva.DetalleNoche;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import com.jefferson.notifications.domain.model.reserva.ReservaPortal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaService extends ServicioBase implements ReservaUseCase {

    public ReservaService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public List<Apartamento> consultarDisponibles(LocalDate entrada, LocalDate salida, int personas) {
        return ejecutar(alojamiento -> alojamiento.consultarDisponibles(entrada, salida, personas));
    }

    @Override
    public List<DetalleNoche> cotizar(String codigoApartamento, LocalDate entrada, LocalDate salida,
                                      List<Ocupante> ocupantes) {
        return ejecutar(alojamiento ->
                alojamiento.cotizar(codigoApartamento, entrada, salida, new ArrayList<>(ocupantes)));
    }

    @Override
    public Reserva crearPortal(String documentoTitular, String codigoApartamento, LocalDate entrada,
                               LocalDate salida, List<Ocupante> ocupantes) {
        return ejecutar(alojamiento -> {
            ArrayList<Ocupante> grupo = new ArrayList<>(ocupantes);
            Reserva reserva = new ReservaPortal(siguienteCodigo(alojamiento),
                    alojamiento.buscarHuesped(documentoTitular), alojamiento.buscarApartamento(codigoApartamento),
                    entrada, salida, grupo, alojamiento.cotizar(codigoApartamento, entrada, salida, grupo),
                    alojamiento.getPoliticaCancelacion());
            alojamiento.crearReserva(reserva);
            return reserva;
        });
    }

    @Override
    public Reserva crearDirecta(String documentoTitular, String codigoApartamento, LocalDate entrada,
                                LocalDate salida, List<Ocupante> ocupantes) {
        return ejecutar(alojamiento -> {
            ArrayList<Ocupante> grupo = new ArrayList<>(ocupantes);
            Reserva reserva = new ReservaDirecta(siguienteCodigo(alojamiento),
                    alojamiento.buscarHuesped(documentoTitular), alojamiento.buscarApartamento(codigoApartamento),
                    entrada, salida, grupo, alojamiento.cotizar(codigoApartamento, entrada, salida, grupo),
                    alojamiento.getPoliticaCancelacion());
            alojamiento.crearReserva(reserva);
            return reserva;
        });
    }

    @Override
    public Reserva recibirExterna(String documentoTitular, String codigoApartamento, LocalDate entrada,
                                  LocalDate salida, List<Ocupante> ocupantes, String codigoExterno,
                                  String plataforma) {
        return ejecutar(alojamiento -> {
            ArrayList<Ocupante> grupo = new ArrayList<>(ocupantes);
            ReservaExterna reserva = new ReservaExterna(siguienteCodigo(alojamiento),
                    alojamiento.buscarHuesped(documentoTitular), alojamiento.buscarApartamento(codigoApartamento),
                    entrada, salida, grupo, alojamiento.cotizar(codigoApartamento, entrada, salida, grupo),
                    alojamiento.getPoliticaCancelacion(), codigoExterno, plataforma);
            alojamiento.recibirReservaExterna(reserva);
            return reserva;
        });
    }

    @Override
    public List<Reserva> listar() {
        return ejecutar(alojamiento -> new ArrayList<>(alojamiento.getReservas()));
    }

    @Override
    public List<Reserva> reservasDelHuesped(String documento) {
        return ejecutar(alojamiento -> alojamiento.consultarReservasHuesped(documento));
    }

    @Override
    public Reserva buscar(String codigo) {
        return ejecutar(alojamiento -> alojamiento.buscarReserva(codigo));
    }

    @Override
    public Reserva confirmar(String codigo) {
        return ejecutar(alojamiento -> {
            alojamiento.confirmarReserva(codigo);
            return alojamiento.buscarReserva(codigo);
        });
    }

    @Override
    public ResultadoCancelacion simularCancelacion(String codigo) {
        return ejecutar(alojamiento -> {
            Reserva reserva = alojamiento.buscarReserva(codigo);
            long devolucion = alojamiento.calcularDevolucion(codigo, LocalDateTime.now());
            return resultado(reserva, reserva.getFolio().calcularTotalPagos(), devolucion);
        });
    }

    @Override
    public ResultadoCancelacion cancelar(String codigo) {
        return ejecutar(alojamiento -> {
            Reserva reserva = alojamiento.buscarReserva(codigo);
            long pagado = reserva.getFolio().calcularTotalPagos();
            long devolucion = alojamiento.cancelarReserva(codigo, LocalDateTime.now());
            return resultado(reserva, pagado, devolucion);
        });
    }

    @Override
    public Reserva hacerCheckIn(String codigo) {
        return ejecutar(alojamiento -> {
            alojamiento.hacerCheckIn(codigo, LocalDateTime.now());
            return alojamiento.buscarReserva(codigo);
        });
    }

    @Override
    public Reserva hacerCheckOut(String codigo) {
        return ejecutar(alojamiento -> {
            alojamiento.hacerCheckOut(codigo);
            return alojamiento.buscarReserva(codigo);
        });
    }

    @Override
    public Reserva declararNoShow(String codigo) {
        return ejecutar(alojamiento -> {
            alojamiento.declararNoShow(codigo, LocalDateTime.now());
            return alojamiento.buscarReserva(codigo);
        });
    }

    @Override
    public List<ConflictoCanal> conflictos() {
        return ejecutar(alojamiento -> new ArrayList<>(alojamiento.getConflictosCanal()));
    }

    @Override
    public int cancelarPendientesVencidas(LocalDateTime ahora) {
        return ejecutar(alojamiento -> alojamiento.cancelarPendientesVencidas(ahora));
    }

    /** R-001, R-002...: uno más que el número más alto que ya exista. */
    private String siguienteCodigo(Alojamiento alojamiento) {
        int mayor = 0;
        for (Reserva reserva : alojamiento.getReservas()) {
            try {
                mayor = Math.max(mayor, Integer.parseInt(reserva.getCodigo().substring(2)));
            } catch (RuntimeException codigoSinNumero) {
                // Un código con otro formato no cuenta
            }
        }
        return String.format("R-%03d", mayor + 1);
    }

    /** Arma la respuesta de la cancelación con la regla de la política congelada que se aplicó. */
    private ResultadoCancelacion resultado(Reserva reserva, long pagado, long devolucion) {
        PoliticaCancelacion politica = reserva.getPolitica();
        long retencionPolitica = politica.calcularRetencion(reserva.getValorEstancia(), reserva.getFechaEntrada(),
                LocalDateTime.now());
        String regla;
        if (retencionPolitica == 0) {
            regla = "Más de " + politica.getDiasDevolucionTotal() + " días de antelación: se devuelve todo lo pagado";
        } else if (retencionPolitica >= reserva.getValorEstancia()) {
            regla = "Menos de " + politica.getHorasRetencionTotal() + " horas para la entrada: se retiene el 100 %";
        } else {
            regla = "Entre " + politica.getHorasRetencionTotal() + " horas y " + politica.getDiasDevolucionTotal()
                    + " días: se retiene el " + politica.getPorcentajeRetencionParcial() + " % de la estancia";
        }
        return new ResultadoCancelacion(pagado, pagado - devolucion, devolucion, regla);
    }
}
