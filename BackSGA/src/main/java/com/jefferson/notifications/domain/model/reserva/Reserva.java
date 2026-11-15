package com.jefferson.notifications.domain.model.reserva;

import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.comun.IRangoFechas;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.folio.Folio;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class Reserva implements IRangoFechas {

    private static final int HORAS_PARA_CONFIRMAR = 24;

    protected String codigo;
    protected Huesped titular;
    protected Apartamento apartamento;
    protected LocalDate fechaEntrada;
    protected LocalDate fechaSalida;
    protected LocalDateTime fechaCreacion;
    protected EstadoReserva estado = EstadoReserva.PENDIENTE;
    protected ArrayList<Ocupante> ocupantes;
    protected ArrayList<DetalleNoche> detalleNoches;
    protected long valorEstancia;            // congelado
    protected PoliticaCancelacion politica;  // congelada
    protected Folio folio;

    public Reserva(String codigo, Huesped titular, Apartamento apartamento, LocalDate fechaEntrada,
                   LocalDate fechaSalida, ArrayList<Ocupante> ocupantes,
                   ArrayList<DetalleNoche> detalleNoches, PoliticaCancelacion politica) {
        this.codigo = codigo;
        this.titular = titular;
        this.apartamento = apartamento;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.fechaCreacion = LocalDateTime.now();
        this.folio = new Folio();

        if (ocupantes == null) {
            this.ocupantes = new ArrayList<>();
        } else {
            this.ocupantes = ocupantes;
        }

        if (detalleNoches == null) {
            this.detalleNoches = new ArrayList<>();
        } else {
            this.detalleNoches = detalleNoches;
        }

        long total = 0;
        for (int i = 0; i < this.detalleNoches.size(); i++) {
            total = total + this.detalleNoches.get(i).calcularSubtotal();
        }
        this.valorEstancia = total;

        if (politica == null) {
            this.politica = new PoliticaCancelacion();
        } else {
            this.politica = politica.copiar();
        }
    }

    public int calcularNoches() {
        if (fechaEntrada == null || fechaSalida == null) {
            return 0;
        }

        int noches = 0;
        LocalDate fecha = fechaEntrada;
        while (fecha.isBefore(fechaSalida)) {
            fecha = fecha.plusDays(1);
            noches = noches + 1;
        }
        return noches;
    }

    public int contarPagadores() {
        int cantidad = 0;
        for (int i = 0; i < ocupantes.size(); i++) {
            Ocupante ocupante = ocupantes.get(i);
            if (ocupante.esPagador(fechaEntrada)) {
                cantidad = cantidad + 1;
            }
        }
        return cantidad;
    }


    public boolean estaActiva() {
        return estado.esActivo();
    }

    public boolean cambiarEstado(EstadoReserva nuevo) {
        if (nuevo == null) {
            return false;
        }
        if (!estado.puedePasarA(nuevo)) {
            return false;
        }
        this.estado = nuevo;
        return true;
    }

    public boolean estaVencida(LocalDateTime ahora) {
        if (ahora == null) {
            return false;
        }
        if (estado != EstadoReserva.PENDIENTE) {
            return false;
        }

        LocalDateTime limite = fechaCreacion.plusHours(HORAS_PARA_CONFIRMAR);
        if (ahora.isAfter(limite)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean contieneNoche(LocalDate noche) {
        if (noche == null) {
            return false;
        }

        boolean desdeLaEntrada = noche.isEqual(fechaEntrada) || noche.isAfter(fechaEntrada);
        boolean antesDeLaSalida = noche.isBefore(fechaSalida);

        if (desdeLaEntrada && antesDeLaSalida) {
            return true;
        }
        return false;
    }

    @Override
    public boolean seCruzaCon(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            return false;
        }

        if (fechaEntrada.isBefore(salida) && entrada.isBefore(fechaSalida)) {
            return true;
        }
        return false;
    }

    public abstract boolean puedeConfirmarse();

    public abstract Canal getCanal();

    public long calcularComision() {
        return 0;
    }


    @Override
    public LocalDate getFechaInicio() {
        return fechaEntrada;
    }

    @Override
    public LocalDate getFechaFin() {
        return fechaSalida;
    }

    public String getCodigo() {
        return codigo;
    }

    public Huesped getTitular() {
        return titular;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public ArrayList<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public ArrayList<DetalleNoche> getDetalleNoches() {
        return detalleNoches;
    }

    public long getValorEstancia() {
        return valorEstancia;
    }

    public PoliticaCancelacion getPolitica() {
        return politica;
    }

    public Folio getFolio() {
        return folio;
    }
}