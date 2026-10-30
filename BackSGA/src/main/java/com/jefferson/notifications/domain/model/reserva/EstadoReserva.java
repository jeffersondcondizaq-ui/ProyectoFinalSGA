package com.jefferson.notifications.domain.model.reserva;

public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA,
    NO_SHOW;

    // Validamos que el cmabio de estado este permitido
    public boolean puedePasarA(EstadoReserva nuevo){
        if (nuevo == null){
            return false;
        }
        return switch (this){
            case PENDIENTE -> nuevo == CONFIRMADA || nuevo == CANCELADA;
            case CONFIRMADA -> nuevo == EN_CURSO || nuevo == CANCELADA || nuevo == NO_SHOW;
            case EN_CURSO -> nuevo == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }

    // estos son los estados en los que una reserva esta activa
    public boolean esActivo() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

}
