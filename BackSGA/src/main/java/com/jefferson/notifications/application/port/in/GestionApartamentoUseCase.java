package com.jefferson.notifications.application.port.in;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoNovedad;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.persona.Usuario;

import java.util.List;
import java.util.Map;

/** Puerto de entrada: apartamentos, tarifas, bloqueos, estado operativo y novedades. */
public interface GestionApartamentoUseCase {

    List<Apartamento> listar(boolean incluirInactivos);

    Apartamento buscar(String codigo);

    Apartamento registrar(Apartamento apartamento);

    Apartamento actualizar(String codigo, String nombre, Integer numDormitorios, Integer capacidad,
                           List<String> caracteristicas);

    void eliminar(String codigo);

    Apartamento definirTarifas(String codigo, Map<TipoTemporada, Long> tarifas);

    void registrarBloqueo(String codigo, Bloqueo bloqueo);

    void cambiarEstado(String codigo, EstadoApartamento estado, String motivo);

    void registrarNovedad(String codigo, TipoNovedad tipo, String descripcion, Usuario reportadaPor);
}
