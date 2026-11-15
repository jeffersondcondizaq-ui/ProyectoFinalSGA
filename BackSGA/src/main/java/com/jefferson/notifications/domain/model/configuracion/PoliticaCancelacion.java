package com.jefferson.notifications.domain.model.configuracion;

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

        // Se cuentan los días que faltan desde hoy hasta la fecha de entrada
        int diasQueFaltan = 0;
        LocalDate hoy = ahora.toLocalDate();
        while (hoy.isBefore(entrada)) {
            hoy = hoy.plusDays(1);
            diasQueFaltan = diasQueFaltan + 1;
        }

        // Las horas de retención total se pasan a días (48 horas = 2 días)
        int diasRetencionTotal = horasRetencionTotal / 24;

        // Con mucha antelación se devuelve todo: no se retiene nada
        if (diasQueFaltan > diasDevolucionTotal) {
            return 0;
        }

        // Muy sobre el tiempo (o ya pasó la fecha, como en el no-show): se retiene todo
        if (diasQueFaltan < diasRetencionTotal) {
            return valorEstancia;
        }

        // En el medio se retiene el porcentaje parcial (por defecto el 50 %)
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