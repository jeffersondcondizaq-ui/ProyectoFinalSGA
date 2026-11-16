package com.jefferson.notifications.domain.model.configuracion;

import com.jefferson.notifications.domain.model.Alojamiento;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class PoliticaCancelacion {

    private int diasDevolucionTotal = 7;
    private int horasRetencionTotal = 48;
    private int porcentajeRetencionParcial = 50;

    public PoliticaCancelacion(int diasDevolucionTotal, int horasRetencionTotal, int porcentajeRetencionParcial) {
        this.diasDevolucionTotal = diasDevolucionTotal;
        this.horasRetencionTotal = horasRetencionTotal;
        this.porcentajeRetencionParcial = porcentajeRetencionParcial;
    }

    public PoliticaCancelacion() {
    }


    public long calcularRetencion(long valorEstancia, LocalDate entrada, LocalDateTime ahora) {
        if (entrada == null || ahora == null) {
            return valorEstancia;
        }

        LocalDateTime horaDeEntrada = entrada.atTime(Alojamiento.HORA_ENTRADA);
        long horasQueFaltan = Duration.between(ahora, horaDeEntrada).toHours();

        if (horasQueFaltan > diasDevolucionTotal * 24L) {
            return 0;
        }
        if (horasQueFaltan < horasRetencionTotal) {
            return valorEstancia;
        }
        return valorEstancia * porcentajeRetencionParcial / 100;
    }

    public long calcularDevolucion(long pagado, long valorEstancia, LocalDate entrada, LocalDateTime ahora) {
        long retencion = calcularRetencion(valorEstancia, entrada, ahora);
        long devolucion = pagado - retencion;

        if (devolucion < 0) {
            devolucion = 0;
        }
        return devolucion;
    }


    public PoliticaCancelacion copiar() {
        return new PoliticaCancelacion(diasDevolucionTotal, horasRetencionTotal, porcentajeRetencionParcial);
    }

    public int getDiasDevolucionTotal() {
        return diasDevolucionTotal;
    }

    public void setDiasDevolucionTotal(int diasDevolucionTotal) {
        if (diasDevolucionTotal < 0) {
            this.diasDevolucionTotal = 0;
        } else {
            this.diasDevolucionTotal = diasDevolucionTotal;
        }
    }

    public int getHorasRetencionTotal() {
        return horasRetencionTotal;
    }

    public void setHorasRetencionTotal(int horasRetencionTotal) {
        if (horasRetencionTotal < 0) {
            this.horasRetencionTotal = 0;
        } else {
            this.horasRetencionTotal = horasRetencionTotal;
        }
    }

    public int getPorcentajeRetencionParcial() {
        return porcentajeRetencionParcial;
    }

    public void setPorcentajeRetencionParcial(int porcentajeRetencionParcial) {
        if (porcentajeRetencionParcial < 0) {
            this.porcentajeRetencionParcial = 0;
        } else if (porcentajeRetencionParcial > 100) {
            this.porcentajeRetencionParcial = 100;
        } else {
            this.porcentajeRetencionParcial = porcentajeRetencionParcial;
        }
    }
}