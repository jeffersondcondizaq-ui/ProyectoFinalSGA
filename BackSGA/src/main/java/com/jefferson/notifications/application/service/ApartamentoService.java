package com.jefferson.notifications.application.service;

import com.jefferson.notifications.application.port.in.GestionApartamentoUseCase;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.Novedad;
import com.jefferson.notifications.domain.model.apartamento.TipoNovedad;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.persona.Recepcionista;
import com.jefferson.notifications.domain.model.persona.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApartamentoService extends ServicioBase implements GestionApartamentoUseCase {

    public ApartamentoService(AlojamientoRepositoryPort repositorio) {
        super(repositorio);
    }

    @Override
    public List<Apartamento> listar(boolean incluirInactivos) {
        return ejecutar(alojamiento -> {
            List<Apartamento> lista = new ArrayList<>();
            for (Apartamento apartamento : alojamiento.getApartamentos()) {
                if (incluirInactivos || apartamento.isActivo()) {
                    lista.add(apartamento);
                }
            }
            return lista;
        });
    }

    @Override
    public Apartamento buscar(String codigo) {
        return ejecutar(alojamiento -> alojamiento.buscarApartamento(codigo));
    }

    @Override
    public Apartamento registrar(Apartamento apartamento) {
        return ejecutar(alojamiento -> {
            alojamiento.registrarApartamento(apartamento);
            return apartamento;
        });
    }

    @Override
    public Apartamento actualizar(String codigo, String nombre, Integer numDormitorios, Integer capacidad,
                                  List<String> caracteristicas) {
        return ejecutar(alojamiento -> {
            Apartamento apartamento = alojamiento.buscarApartamento(codigo);
            alojamiento.actualizarApartamento(codigo,
                    nombre != null ? nombre : apartamento.getNombre(),
                    capacidad != null ? capacidad : apartamento.getCapacidadMaxima());
            if (numDormitorios != null) {
                apartamento.setNumDormitorios(numDormitorios);
            }
            if (caracteristicas != null) {
                apartamento.setCaracteristicas(new ArrayList<>(caracteristicas));
            }
            return apartamento;
        });
    }

    @Override
    public void eliminar(String codigo) {
        ejecutar(alojamiento -> alojamiento.eliminarApartamento(codigo));
    }

    @Override
    public Apartamento definirTarifas(String codigo, Map<TipoTemporada, Long> tarifas) {
        return ejecutar(alojamiento -> {
            Apartamento apartamento = alojamiento.buscarApartamento(codigo);
            tarifas.forEach((temporada, valor) -> {
                if (valor == null || valor <= 0) {
                    throw new ReglaNegocioException("La tarifa de temporada " + temporada + " debe ser mayor que cero");
                }
                apartamento.definirTarifa(temporada, valor);
            });
            return apartamento;
        });
    }

    @Override
    public void registrarBloqueo(String codigo, Bloqueo bloqueo) {
        ejecutar(alojamiento -> alojamiento.registrarBloqueo(codigo, bloqueo));
    }

    @Override
    public void cambiarEstado(String codigo, EstadoApartamento estado, String motivo) {
        ejecutar(alojamiento -> {
            Apartamento apartamento = alojamiento.buscarApartamento(codigo);
            if (estado == EstadoApartamento.OCUPADO) {
                throw new ReglaNegocioException("El estado Ocupado lo asigna el check-in, no se cambia a mano");
            }
            if (apartamento.getEstado() == EstadoApartamento.OCUPADO) {
                throw new ReglaNegocioException("El apartamento " + codigo + " está Ocupado: primero se hace el check-out");
            }
            if (!apartamento.cambiarEstado(estado, motivo)) {
                throw new ReglaNegocioException("Para pasar a Fuera de servicio hay que registrar el motivo");
            }
            return null;
        });
    }

    @Override
    public void registrarNovedad(String codigo, TipoNovedad tipo, String descripcion, Usuario reportadaPor) {
        ejecutar(alojamiento -> {
            if (tipo == null || descripcion == null || descripcion.isBlank()) {
                throw new ReglaNegocioException("La novedad debe tener tipo y descripción");
            }
            Recepcionista recepcionista = reportadaPor instanceof Recepcionista r ? r : null;
            alojamiento.buscarApartamento(codigo).registrarNovedad(new Novedad(tipo, descripcion, recepcionista));
            return null;
        });
    }
}
