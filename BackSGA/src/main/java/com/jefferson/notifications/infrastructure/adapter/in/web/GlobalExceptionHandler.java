package com.jefferson.notifications.infrastructure.adapter.in.web;

import com.jefferson.notifications.application.exception.NoAutorizadoException;
import com.jefferson.notifications.domain.exception.RecursoNoEncontradoException;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.infrastructure.adapter.in.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte las excepciones en respuestas HTTP con el cuerpo { "mensaje": "..." } */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 409: la operación viola una regla del negocio (doble venta, capacidad, estado...). */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse reglaDeNegocio(ReglaNegocioException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** 404: el apartamento, la reserva, el huésped... no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(RecursoNoEncontradoException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** 401: credenciales incorrectas, sin token o token inválido. */
    @ExceptionHandler(NoAutorizadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse noAutorizado(NoAutorizadoException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** 400: falló una validación (@NotBlank, @Size...): se muestra el primer mensaje. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse datosInvalidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Los datos enviados no son válidos");
        return new ErrorResponse(mensaje);
    }

    /** 400: el JSON está mal formado o una fecha no viene como AAAA-MM-DD. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse cuerpoIlegible(HttpMessageNotReadableException e) {
        return new ErrorResponse("El cuerpo de la petición no es válido (las fechas van como AAAA-MM-DD)");
    }

    /** 400: falta un parámetro o una cabecera, o vienen con un tipo que no corresponde. */
    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parametroInvalido(Exception e) {
        return new ErrorResponse("Parámetro inválido: " + e.getMessage());
    }

    /** 404: la URL no existe en el API. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse rutaInexistente(NoResourceFoundException e) {
        return new ErrorResponse("No existe la ruta /" + e.getResourcePath());
    }

    /** 405: la URL existe pero no con ese método (por ejemplo GET en vez de POST). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return new ErrorResponse("Método " + e.getMethod() + " no permitido en esta ruta");
    }

    /** 500: cualquier otro error. Se registra completo en la consola del back. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse inesperado(Exception e) {
        log.error("Error inesperado", e);
        return new ErrorResponse("Ocurrió un error inesperado en el servidor");
    }
}