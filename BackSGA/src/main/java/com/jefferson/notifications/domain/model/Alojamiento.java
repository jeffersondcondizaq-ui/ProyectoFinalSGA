package com.jefferson.notifications.domain.model;

import com.jefferson.notifications.domain.exception.RecursoNoEncontradoException;
import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PoliticaCancelacion;
import com.jefferson.notifications.domain.model.persona.Empleado;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Usuario;
import com.jefferson.notifications.domain.model.reserva.ConflictoCanal;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.reserva.DetalleNoche;

import java.time.LocalDate;
import java.util.ArrayList;

public class Alojamiento {

    private String nombre;
    private String ubicacion;
    private ArrayList<Apartamento> apartamentos;
    private ArrayList<Huesped> huespedes;
    private ArrayList<Empleado> empleados;
    private ArrayList<Reserva> reservas;
    private ArrayList<PeriodoTemporada> periodosTemporada;
    private PoliticaCancelacion politicaCancelacion;
    private ArrayList<ConflictoCanal> conflictosCanal;
    private Usuario sesionActual;

    public Alojamiento(String nombre, String ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.apartamentos = new ArrayList<>();
        this.huespedes = new ArrayList<>();
        this.empleados = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.periodosTemporada = new ArrayList<>();
        this.politicaCancelacion = new PoliticaCancelacion();
        this.conflictosCanal = new ArrayList<>();
    }

    public Usuario iniciarSesion(String usuario, String contrasena) {
        Usuario encontrado = buscarUsuario(usuario);

        if (encontrado == null || !encontrado.autenticar(usuario, contrasena)) {
            throw new ReglaNegocioException("Usuario o contraseña incorrectos");
        }

        // Un empleado desvinculado ya no puede entrar
        if (encontrado instanceof Empleado empleado && !empleado.isActivo()) {
            throw new ReglaNegocioException("El empleado está desvinculado");
        }

        this.sesionActual = encontrado;
        return encontrado;
    }

    public void cerrarSesion() {
        this.sesionActual = null;
    }

    public boolean registrarHuesped(Huesped huesped) {
        if (huesped == null) {
            throw new ReglaNegocioException("El huésped es obligatorio");
        }
        validarUsuarioNuevo(huesped);
        huespedes.add(huesped);
        return true;
    }

    public Huesped buscarHuesped(String documento) {
        for (Huesped huesped : huespedes) {
            if (huesped.getDocumento().equals(documento)) {
                return huesped;
            }
        }
        throw new RecursoNoEncontradoException("No existe un huésped con documento " + documento);
    }

    public boolean contratarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new ReglaNegocioException("El empleado es obligatorio");
        }
        validarUsuarioNuevo(empleado);
        empleados.add(empleado);
        return true;
    }

    public Empleado buscarEmpleado(String documento) {
        for (Empleado empleado : empleados) {
            if (empleado.getDocumento().equals(documento)) {
                return empleado;
            }
        }
        throw new RecursoNoEncontradoException("No existe un empleado con documento " + documento);
    }

    public boolean actualizarEmpleado(String documento, String telefono, String correo) {
        Empleado empleado = buscarEmpleado(documento);
        if (telefono == null || telefono.isEmpty() || correo == null || !correo.contains("@")) {
            throw new ReglaNegocioException("Teléfono y correo válidos son obligatorios");
        }
        empleado.setTelefono(telefono);
        empleado.setCorreo(correo);
        return true;
    }

    public boolean desvincularEmpleado(String documento) {
        Empleado empleado = buscarEmpleado(documento);
        if (!empleado.isActivo()) {
            throw new ReglaNegocioException("El empleado ya estaba desvinculado");
        }
        empleado.desvincular();
        return true;
    }

    public boolean registrarApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new ReglaNegocioException("El apartamento es obligatorio");
        }
        for (Apartamento existente : apartamentos) {
            if (existente.getCodigo().equals(apartamento.getCodigo())) {
                throw new ReglaNegocioException("Ya existe un apartamento con código " + apartamento.getCodigo());
            }
        }
        if (apartamento.getCapacidadMaxima() <= 0) {
            throw new ReglaNegocioException("La capacidad máxima debe ser mayor que cero");
        }
        apartamentos.add(apartamento);
        return true;
    }

    /**
     * Registra una reserva nueva (de cualquier canal) después de validar las reglas.
     * Las reglas se van sumando en cada tarjeta: por ahora fechas y capacidad.
     */
    public boolean crearReserva(Reserva reserva) {
        if (reserva == null) {
            throw new ReglaNegocioException("La reserva es obligatoria");
        }
        validarFechas(reserva.getFechaEntrada(), reserva.getFechaSalida());
        validarCapacidad(reserva.getApartamento(), reserva.getOcupantes().size());
        if (!estaDisponible(reserva.getApartamento(), reserva.getFechaEntrada(), reserva.getFechaSalida())) {
            throw new ReglaNegocioException("El apartamento " + reserva.getApartamento().getCodigo()
                    + " no está disponible en esas fechas");
        }
        reservas.add(reserva);
        return true;
    }

    /**
     * Regla "sin doble venta"
     */
    public boolean estaDisponible(Apartamento apto, LocalDate entrada, LocalDate salida) {
        if (apto == null || entrada == null || salida == null) {
            return false;
        }
        if (!apto.isActivo() || apto.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
            return false;
        }
        LocalDate noche = entrada;
        while (noche.isBefore(salida)) {
            if (apto.estaBloqueado(noche)) {
                return false;
            }
            noche = noche.plusDays(1);
        }
        for (Reserva reserva : reservasActivasDe(apto)) {
            if (reserva.seCruzaCon(entrada, salida)) {
                return false;
            }
        }
        return true;
    }

    /** Apartamentos que admiten esa cantidad de personas y están libres en esas fechas. */
    public ArrayList<Apartamento> consultarDisponibles(LocalDate entrada, LocalDate salida, int personas) {
        validarFechas(entrada, salida);
        if (personas <= 0) {
            throw new ReglaNegocioException("La cantidad de personas debe ser mayor que cero");
        }
        ArrayList<Apartamento> disponibles = new ArrayList<>();
        for (Apartamento apto : apartamentos) {
            if (apto.admiteOcupantes(personas) && estaDisponible(apto, entrada, salida)) {
                disponibles.add(apto);
            }
        }
        return disponibles;
    }

    public Apartamento buscarApartamento(String codigo) {
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.getCodigo().equals(codigo)) {
                return apartamento;
            }
        }
        throw new RecursoNoEncontradoException("No existe el apartamento " + codigo);
    }

    public boolean actualizarApartamento(String codigo, String nombre, int capacidad) {
        Apartamento apartamento = buscarApartamento(codigo);
        if (nombre == null || nombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (capacidad <= 0) {
            throw new ReglaNegocioException("La capacidad máxima debe ser mayor que cero");
        }
        apartamento.setNombre(nombre);
        apartamento.setCapacidadMaxima(capacidad);
        return true;
    }

    /** Eliminación lógica: deja de venderse, pero conserva su historial. */
    public boolean eliminarApartamento(String codigo) {
        Apartamento apartamento = buscarApartamento(codigo);
        if (!reservasActivasDe(apartamento).isEmpty()) {
            throw new ReglaNegocioException("El apartamento " + codigo + " tiene reservas activas");
        }
        apartamento.desactivar();
        return true;
    }

    public boolean registrarBloqueo(String codigoApto, Bloqueo bloqueo) {
        Apartamento apartamento = buscarApartamento(codigoApto);
        if (bloqueo == null || bloqueo.getMotivo() == null || bloqueo.getMotivo().isBlank()) {
            throw new ReglaNegocioException("El bloqueo debe tener un motivo");
        }
        // No se puede bloquear noches que ya están vendidas
        for (Reserva reserva : reservasActivasDe(apartamento)) {
            if (reserva.seCruzaCon(bloqueo.getFechaInicio(), bloqueo.getFechaFin())) {
                throw new ReglaNegocioException("Las fechas se cruzan con la reserva " + reserva.getCodigo());
            }
        }
        if (!apartamento.agregarBloqueo(bloqueo)) {
            throw new ReglaNegocioException("El bloqueo debe cubrir al menos una noche");
        }
        return true;
    }

    public boolean registrarPeriodoTemporada(PeriodoTemporada periodo) {
        if (periodo == null || periodo.getFechaInicio() == null || periodo.getFechaFin() == null) {
            throw new ReglaNegocioException("El periodo debe tener fecha de inicio y de fin");
        }
        if (!periodo.getFechaFin().isAfter(periodo.getFechaInicio())) {
            throw new ReglaNegocioException("El periodo debe cubrir al menos una noche");
        }
        // Baja es la temporada por defecto: no se registra como periodo
        if (periodo.getTipo() == TipoTemporada.BAJA) {
            throw new ReglaNegocioException("Solo se registran periodos de temporada Media o Alta");
        }
        for (PeriodoTemporada existente : periodosTemporada) {
            if (existente.seCruzaCon(periodo.getFechaInicio(), periodo.getFechaFin())) {
                throw new ReglaNegocioException("El periodo se cruza con " + existente.getDescripcion());
            }
        }
        periodosTemporada.add(periodo);
        return true;
    }

    /** Temporada de una noche: la del periodo que la contiene, o Baja si ninguno la contiene. */
    public TipoTemporada obtenerTemporada(LocalDate noche) {
        for (PeriodoTemporada periodo : periodosTemporada) {
            if (periodo.contieneNoche(noche)) {
                return periodo.getTipo();
            }
        }
        return TipoTemporada.BAJA;
    }

    /** Solo afecta a las reservas nuevas: cada reserva guarda una copia al crearse. */
    public void configurarPolitica(PoliticaCancelacion politica) {
        if (politica == null) {
            throw new ReglaNegocioException("La política es obligatoria");
        }
        this.politicaCancelacion = politica;
    }

    /**
     * Regla "valor noche por noche"
     */
    public ArrayList<DetalleNoche> cotizar(String codigoApto, LocalDate entrada, LocalDate salida,
                                           ArrayList<Ocupante> ocupantes) {
        Apartamento apto = buscarApartamento(codigoApto);
        validarFechas(entrada, salida);
        if (ocupantes == null) {
            ocupantes = new ArrayList<>();
        }
        validarCapacidad(apto, ocupantes.size());

        int pagadores = 0;
        for (Ocupante ocupante : ocupantes) {
            if (ocupante.esPagador(entrada)) {
                pagadores++;
            }
        }

        ArrayList<DetalleNoche> detalle = new ArrayList<>();
        LocalDate noche = entrada;
        while (noche.isBefore(salida)) {
            TipoTemporada temporada = obtenerTemporada(noche);
            long tarifa = apto.getTarifa(temporada);
            if (tarifa <= 0) {
                throw new ReglaNegocioException("El apartamento " + apto.getCodigo()
                        + " no tiene tarifa definida para temporada " + temporada);
            }
            detalle.add(new DetalleNoche(noche, temporada, tarifa, pagadores));
            noche = noche.plusDays(1);
        }
        return detalle;
    }

    public Reserva buscarReserva(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new RecursoNoEncontradoException("No existe la reserva " + codigo);
    }

    public ArrayList<Reserva> consultarReservasHuesped(String documento) {
        ArrayList<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getTitular().getDocumento().equals(documento)) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    /** Busca un usuario (huésped o empleado) por su nombre de usuario; null si no existe. */
    private Usuario buscarUsuario(String usuario) {
        for (Huesped huesped : huespedes) {
            if (huesped.getUsuario().equals(usuario)) {
                return huesped;
            }
        }
        for (Empleado empleado : empleados) {
            if (empleado.getUsuario().equals(usuario)) {
                return empleado;
            }
        }
        return null;
    }

    /** Documento y usuario no se pueden repetir entre huéspedes y empleados. */
    private void validarUsuarioNuevo(Usuario nuevo) {
        if (buscarUsuario(nuevo.getUsuario()) != null) {
            throw new ReglaNegocioException("El usuario " + nuevo.getUsuario() + " ya existe");
        }
        for (Huesped huesped : huespedes) {
            if (huesped.getDocumento().equals(nuevo.getDocumento())) {
                throw new ReglaNegocioException("Ya existe un huésped con documento " + nuevo.getDocumento());
            }
        }
        for (Empleado empleado : empleados) {
            if (empleado.getDocumento().equals(nuevo.getDocumento())) {
                throw new ReglaNegocioException("Ya existe un empleado con documento " + nuevo.getDocumento());
            }
        }
    }

    /**
     * Regla "fechas válidas": la salida debe ser posterior a la entrada (mínimo una noche)
     * y no se puede reservar con fecha de entrada anterior a hoy.
     */
    private void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new ReglaNegocioException("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            throw new ReglaNegocioException("La salida debe ser posterior a la entrada (mínimo una noche)");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No se puede reservar con fecha de entrada anterior a hoy");
        }
    }

    /**
     * Regla "capacidad máxima": cuentan todos los ocupantes, incluidos los menores que no pagan.
     */
    private void validarCapacidad(Apartamento apartamento, int cantidadOcupantes) {
        if (apartamento == null) {
            throw new ReglaNegocioException("El apartamento es obligatorio");
        }
        if (cantidadOcupantes <= 0) {
            throw new ReglaNegocioException("La reserva debe tener al menos un ocupante");
        }
        if (!apartamento.admiteOcupantes(cantidadOcupantes)) {
            throw new ReglaNegocioException("El apartamento " + apartamento.getCodigo() + " admite máximo "
                    + apartamento.getCapacidadMaxima() + " personas");
        }
    }

    /** Reservas Pendientes, Confirmadas o En curso de un apartamento. */
    private ArrayList<Reserva> reservasActivasDe(Apartamento apartamento) {
        ArrayList<Reserva> activas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getApartamento() == apartamento && reserva.estaActiva()) {
                activas.add(reserva);
            }
        }
        return activas;
    }


    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public ArrayList<Apartamento> getApartamentos() {
        return apartamentos;
    }

    public ArrayList<Huesped> getHuespedes() {
        return huespedes;
    }

    public ArrayList<Empleado> getEmpleados() {
        return empleados;
    }

    public ArrayList<Reserva> getReservas() {
        return reservas;
    }

    public ArrayList<PeriodoTemporada> getPeriodosTemporada() {
        return periodosTemporada;
    }

    public PoliticaCancelacion getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public ArrayList<ConflictoCanal> getConflictosCanal() {
        return conflictosCanal;
    }

    public Usuario getSesionActual() {
        return sesionActual;
    }
}