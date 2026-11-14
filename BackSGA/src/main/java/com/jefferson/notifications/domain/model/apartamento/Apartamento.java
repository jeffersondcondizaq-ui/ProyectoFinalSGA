package com.jefferson.notifications.domain.model.apartamento;

import java.time.LocalDate;
import java.util.ArrayList;

public class Apartamento {
    private String codigo;
    private String nombre;
    private int numDormitorios;
    private int capacidadMaxima;
    private ArrayList<String> caracteristicas;
    private EstadoApartamento estado = EstadoApartamento.PREPARADO;
    private String motivoFueraServicio;
    private boolean activo = true;
    private ArrayList<Tarifa> tarifas;
    private ArrayList<Bloqueo> bloqueos;
    private ArrayList<Novedad> novedades;
    private ArrayList<String> fotos;

    public Apartamento(String codigo, String nombre, int numDormitorios, int capacidadMaxima, ArrayList<String> caracteristicas, EstadoApartamento estado, String motivoFueraServicio, boolean activo, ArrayList<Tarifa> tarifas, ArrayList<Bloqueo> bloqueos, ArrayList<Novedad> novedades, ArrayList<String> fotos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.numDormitorios = numDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = caracteristicas;
        this.estado = estado;
        this.motivoFueraServicio = motivoFueraServicio;
        this.activo = activo;
        this.tarifas = tarifas;
        this.bloqueos = bloqueos;
        this.novedades = novedades;
        this.fotos = fotos;
    }

    // Getters y Setters

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumDormitorios() {
        return numDormitorios;
    }

    public void setNumDormitorios(int numDormitorios) {
        this.numDormitorios = numDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public ArrayList<String> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(ArrayList<String> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public EstadoApartamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoApartamento estado) {
        this.estado = estado;
    }

    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
    }

    public void setMotivoFueraServicio(String motivoFueraServicio) {
        this.motivoFueraServicio = motivoFueraServicio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public ArrayList<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(ArrayList<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public ArrayList<Bloqueo> getBloqueos() {
        return bloqueos;
    }

    public void setBloqueos(ArrayList<Bloqueo> bloqueos) {
        this.bloqueos = bloqueos;
    }

    public ArrayList<Novedad> getNovedades() {
        return novedades;
    }

    public void setNovedades(ArrayList<Novedad> novedades) {
        this.novedades = novedades;
    }

    public ArrayList<String> getFotos() {
        return fotos;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.fotos = fotos;
    }

    // Metodos
    public void definirTarifa(TipoTemporada tipo, long valor) {
        if (tipo == null) {
            return;
        }

        for (int i = 0; i < tarifas.size(); i++) {
            Tarifa tarifa = tarifas.get(i);
            if (tarifa.getTipoTemporada() == tipo) {
                tarifa.setValorPersonaNoche(valor);
                return;
            }
        }

        Tarifa nueva = new Tarifa(tipo, valor);
        tarifas.add(nueva);
    }

    public long getTarifa(TipoTemporada tipo) {
        for (int i = 0; i < tarifas.size(); i++) {
            Tarifa tarifa = tarifas.get(i);
            if (tarifa.getTipoTemporada() == tipo) {
                return tarifa.getValorPersonaNoche();
            }
        }
        return 0;
    }


    public boolean cambiarEstado(EstadoApartamento nuevo, String motivo) {
        if (nuevo == null) {
            return false;
        }

        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {

            if (motivo == null || motivo.isEmpty()) {
                return false;
            }
            this.estado = nuevo;
            this.motivoFueraServicio = motivo;
            return true;
        }

        this.estado = nuevo;
        this.motivoFueraServicio = null;
        return true;
    }

    public boolean estaBloqueado(LocalDate noche) {
        for (int i = 0; i < bloqueos.size(); i++) {
            Bloqueo bloqueo = bloqueos.get(i);
            if (bloqueo.contieneNoche(noche)) {
                return true;
            }
        }
        return false;
    }


    public boolean agregarBloqueo(Bloqueo bloqueo) {
        if (bloqueo == null) {
            return false;
        }

        LocalDate inicio = bloqueo.getFechaInicio();
        LocalDate fin = bloqueo.getFechaFin();

        if (inicio == null || fin == null) {
            return false;
        }

        // El bloqueo debe cubrir al menos una noche
        if (!fin.isAfter(inicio)) {
            return false;
        }

        bloqueos.add(bloqueo);
        return true;
    }

    public void registrarNovedad(Novedad novedad) {
        if (novedad == null) {
            return;
        }
        novedades.add(novedad);
    }


    public boolean admiteOcupantes(int cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        if (cantidad > capacidadMaxima) {
            return false;
        }
        return true;
    }

    public void agregarCaracteristica(String caracteristica) {
        if (caracteristica == null || caracteristica.isEmpty()) {
            return;
        }

        // No se repite una característica que ya esté en la lista
        for (int i = 0; i < caracteristicas.size(); i++) {
            if (caracteristicas.get(i).equals(caracteristica)) {
                return;
            }
        }

        caracteristicas.add(caracteristica);
    }

    public void desactivar() {
        this.activo = false;
    }
}
