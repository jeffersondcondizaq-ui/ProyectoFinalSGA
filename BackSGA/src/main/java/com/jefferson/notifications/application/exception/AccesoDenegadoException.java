package com.jefferson.notifications.application.exception;

/** El usuario tiene sesión, pero su rol no le permite hacer esa operación (403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
