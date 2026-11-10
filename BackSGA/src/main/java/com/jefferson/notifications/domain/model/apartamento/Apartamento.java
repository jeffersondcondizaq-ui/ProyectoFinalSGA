package com.jefferson.notifications.domain.model.apartamento;

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
}
