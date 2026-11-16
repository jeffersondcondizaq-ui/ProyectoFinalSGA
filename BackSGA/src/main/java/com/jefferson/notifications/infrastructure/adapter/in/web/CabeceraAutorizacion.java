package com.jefferson.notifications.infrastructure.adapter.in.web;

public final class CabeceraAutorizacion {

    private static final String PREFIJO = "Bearer ";

    private CabeceraAutorizacion() {
    }

    public static String token(String cabecera) {
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            return null;
        }
        return cabecera.substring(PREFIJO.length()).trim();
    }
}