package com.jefferson.notifications.infrastructure.adapter.out.persistence;

import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.Bloqueo;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.Novedad;
import com.jefferson.notifications.domain.model.apartamento.TipoNovedad;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.folio.Cargo;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.folio.TipoCargo;
import com.jefferson.notifications.domain.model.persona.Administrador;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.persona.Recepcionista;
import com.jefferson.notifications.domain.model.reserva.Reserva;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import com.jefferson.notifications.domain.model.reserva.ReservaPortal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de ejemplo con los que arranca el back, para probar todos los roles y pantallas.
 * Todo se crea con los métodos del dominio, así los datos semilla también cumplen las reglas.
 * Las reservas usan fechas relativas a hoy para que siempre sean válidas.
 *
 * Usuarios (usuario / contraseña):
 *   Administración: admin / admin123, gerencia / gerencia123
 *   Recepción:      recepcion / recepcion123, ctorres / camila123 (pcardenas está desvinculada)
 *   Huéspedes:      laura / laura123, 1053998877 / carlos123, 1010456789 / valentina123,
 *                   jorge / jorge123, mariana / mariana123, andres / andres123, sebastian / sebastian123
 */
public final class DatosSemilla {

    private DatosSemilla() {
    }

    public static Alojamiento crear() {
        Alojamiento alojamiento = new Alojamiento("Aparta-Suites UQ", "Salento, Quindío");
        cargarEmpleados(alojamiento);
        cargarHuespedes(alojamiento);
        cargarApartamentos(alojamiento);
        cargarTemporadas(alojamiento);
        cargarReservas(alojamiento, LocalDate.now());
        cargarNovedadesYBloqueos(alojamiento, LocalDate.now());
        // Estados operativos al final, para no estorbar a las reservas de ejemplo
        alojamiento.buscarApartamento("APT-201").cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, null);
        alojamiento.buscarApartamento("APT-202").cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO,
                "Daño en la tubería del baño principal");
        alojamiento.buscarApartamento("APT-103").cambiarEstado(EstadoApartamento.EN_PREPARACION, null);
        alojamiento.eliminarApartamento("APT-302");
        return alojamiento;
    }

    // ------------------------------------------------------------------
    // Personas
    // ------------------------------------------------------------------

    private static void cargarEmpleados(Alojamiento alojamiento) {
        alojamiento.contratarEmpleado(new Administrador("Diana Marcela Ospina", LocalDate.of(1985, 4, 12),
                "1094000001", "3104567890", "diana.ospina@apartasuites.co", "admin", "admin123",
                LocalDate.of(2022, 2, 1)));
        alojamiento.contratarEmpleado(new Administrador("Ricardo León Valencia", LocalDate.of(1979, 8, 3),
                "1094000004", "3187654321", "ricardo.leon@apartasuites.co", "gerencia", "gerencia123",
                LocalDate.of(2021, 1, 15)));
        alojamiento.contratarEmpleado(new Recepcionista("Julián Andrés Mejía", LocalDate.of(1998, 9, 30),
                "1094000002", "3112223344", "julian.mejia@apartasuites.co", "recepcion", "recepcion123",
                LocalDate.of(2024, 6, 15)));
        alojamiento.contratarEmpleado(new Recepcionista("Camila Torres Giraldo", LocalDate.of(2000, 3, 14),
                "1094000005", "3165554433", "camila.torres@apartasuites.co", "ctorres", "camila123",
                LocalDate.of(2025, 9, 1)));
        alojamiento.contratarEmpleado(new Recepcionista("Paula Andrea Cárdenas", LocalDate.of(1996, 1, 22),
                "1094000003", "3159876543", "paula.cardenas@apartasuites.co", "pcardenas", "paula123",
                LocalDate.of(2023, 3, 1)));
        alojamiento.desvincularEmpleado("1094000003");
    }

    private static void cargarHuespedes(Alojamiento alojamiento) {
        huesped(alojamiento, "Laura Gómez Restrepo", LocalDate.of(1995, 7, 18), "1094123456", "3201234567",
                "laura.gomez@correo.com", "laura", "laura123", 47);
        huesped(alojamiento, "Carlos Ruiz Patiño", LocalDate.of(1988, 11, 23), "1053998877", "3157654321",
                "carlos.ruiz@correo.com", "1053998877", "carlos123", 12);
        huesped(alojamiento, "Valentina Ríos Arango", LocalDate.of(1999, 4, 2), "1010456789", "3009876543",
                "valentina.rios@correo.com", "1010456789", "valentina123", 45);
        huesped(alojamiento, "Jorge Iván Londoño", LocalDate.of(1979, 6, 8), "1094555666", "3014445566",
                "jorge.londono@correo.com", "jorge", "jorge123", 33);
        huesped(alojamiento, "Mariana Castaño Vélez", LocalDate.of(2000, 3, 27), "1094777888", "3027778899",
                "mariana.castano@correo.com", "mariana", "mariana123", 44);
        huesped(alojamiento, "Andrés Felipe Gómez", LocalDate.of(1993, 2, 5), "1094888999", "3051112233",
                "andres.gomez@correo.com", "andres", "andres123", 15);
        // Huésped sin reservas, para probar el flujo de reservar desde cero
        huesped(alojamiento, "Sebastián Henao Ruiz", LocalDate.of(1997, 10, 11), "1094999000", "3123334455",
                "sebastian.henao@correo.com", "sebastian", "sebastian123", 59);
    }

    private static void huesped(Alojamiento alojamiento, String nombre, LocalDate nacimiento, String documento,
                                String telefono, String correo, String usuario, String contrasena, int foto) {
        Huesped huesped = new Huesped(nombre, nacimiento, documento, telefono, correo, usuario, contrasena);
        huesped.setFoto("https://i.pravatar.cc/300?img=" + foto);
        alojamiento.registrarHuesped(huesped);
    }

    // ------------------------------------------------------------------
    // Apartamentos y temporadas
    // ------------------------------------------------------------------

    private static void cargarApartamentos(Alojamiento alojamiento) {
        registrar(alojamiento, "APT-101", "Suite Cocora", 2, 4, 80_000, 100_000, 130_000,
                "Balcón", "Vista a las montañas", "Chimenea");
        registrar(alojamiento, "APT-102", "Loft Palma de Cera", 1, 2, 70_000, 90_000, 115_000,
                "Jacuzzi", "Vista al valle");
        registrar(alojamiento, "APT-103", "Estudio Barbas", 1, 2, 60_000, 75_000, 95_000,
                "Escritorio de trabajo", "Wifi de alta velocidad", "Cocineta");
        registrar(alojamiento, "APT-201", "Casa del Valle", 3, 6, 65_000, 90_000, 120_000,
                "Balcón", "Cocina amplia", "Zona de BBQ");
        registrar(alojamiento, "APT-202", "Mirador del Quindío", 2, 3, 75_000, 95_000, 125_000,
                "Vista a las montañas", "Terraza privada");
        registrar(alojamiento, "APT-203", "Penthouse Salento", 3, 5, 110_000, 140_000, 180_000,
                "Terraza con jacuzzi", "Vista panorámica", "Chimenea", "Cocina completa");
        registrar(alojamiento, "APT-301", "Cabaña del Bosque", 4, 8, 60_000, 80_000, 105_000,
                "Fogata", "Zona verde", "Parqueadero", "Admite mascotas");
        registrar(alojamiento, "APT-302", "Glamping Río Quindío", 1, 2, 90_000, 110_000, 140_000,
                "Domo panorámico", "Hamaca", "Junto al río");
    }

    private static void registrar(Alojamiento alojamiento, String codigo, String nombre, int dormitorios,
                                  int capacidad, long baja, long media, long alta, String... caracteristicas) {
        ArrayList<String> fotos = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            fotos.add("https://picsum.photos/seed/" + codigo.toLowerCase() + "-" + i + "/800/600");
        }
        Apartamento apartamento = new Apartamento(codigo, nombre, dormitorios, capacidad,
                new ArrayList<>(List.of(caracteristicas)), EstadoApartamento.PREPARADO, null, true,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), fotos);
        apartamento.definirTarifa(TipoTemporada.BAJA, baja);
        apartamento.definirTarifa(TipoTemporada.MEDIA, media);
        apartamento.definirTarifa(TipoTemporada.ALTA, alta);
        alojamiento.registrarApartamento(apartamento);
    }

    private static void cargarTemporadas(Alojamiento alojamiento) {
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.MEDIA,
                LocalDate.of(2026, 11, 14), LocalDate.of(2026, 11, 17), "Puente festivo de noviembre"));
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.ALTA,
                LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16), "Vacaciones de diciembre y enero"));
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.ALTA,
                LocalDate.of(2027, 3, 20), LocalDate.of(2027, 3, 29), "Semana Santa"));
        alojamiento.registrarPeriodoTemporada(new PeriodoTemporada(TipoTemporada.MEDIA,
                LocalDate.of(2027, 6, 12), LocalDate.of(2027, 6, 16), "Puente festivo de junio"));
    }

    // ------------------------------------------------------------------
    // Reservas de ejemplo: una por cada estado y canal
    // ------------------------------------------------------------------

    private static void cargarReservas(Alojamiento a, LocalDate hoy) {
        Huesped laura = a.buscarHuesped("1094123456");
        Huesped carlos = a.buscarHuesped("1053998877");
        Huesped valentina = a.buscarHuesped("1010456789");
        Huesped jorge = a.buscarHuesped("1094555666");
        Huesped mariana = a.buscarHuesped("1094777888");
        Huesped andres = a.buscarHuesped("1094888999");

        // R-001 · Portal · Confirmada con el anticipo del 30 % (la niña de 4 años no paga)
        ArrayList<Ocupante> familiaLaura = ocupantes(
                new Ocupante("Laura Gómez Restrepo", LocalDate.of(1995, 7, 18)),
                new Ocupante("Andrés Felipe Gómez", LocalDate.of(1993, 2, 5)),
                new Ocupante("Sofía Gómez", LocalDate.of(2022, 3, 10)));
        ReservaPortal r1 = portal(a, "R-001", laura, "APT-101", hoy.plusDays(5), hoy.plusDays(8), familiaLaura);
        pagar(r1, MedioPago.TARJETA, r1.calcularAnticipo(), "Anticipo 30 %");
        a.confirmarReserva("R-001");

        // R-002 · Directa · En curso (llegó hoy) con un abono; APT-102 queda Ocupado
        ArrayList<Ocupante> parejaCarlos = ocupantes(
                new Ocupante("Carlos Ruiz Patiño", LocalDate.of(1988, 11, 23)),
                new Ocupante("Mónica Salazar", LocalDate.of(1990, 5, 14)));
        Reserva r2 = directa(a, "R-002", carlos, "APT-102", hoy, hoy.plusDays(3), parejaCarlos);
        pagar(r2, MedioPago.EFECTIVO, 200_000, "Abono en recepción");
        a.confirmarReserva("R-002");
        a.hacerCheckIn("R-002", hoy.atTime(Alojamiento.HORA_ENTRADA));
        r2.getFolio().agregarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL, 36_000, "Desayuno x2", false));

        // R-003 · Portal · Pendiente (sin anticipo todavía)
        ArrayList<Ocupante> amigosLaura = ocupantes(
                new Ocupante("Laura Gómez Restrepo", LocalDate.of(1995, 7, 18)),
                new Ocupante("Andrés Felipe Gómez", LocalDate.of(1993, 2, 5)),
                new Ocupante("Camilo Restrepo", LocalDate.of(1996, 8, 30)),
                new Ocupante("Natalia Pérez", LocalDate.of(1997, 12, 1)));
        portal(a, "R-003", laura, "APT-201", hoy.plusDays(10), hoy.plusDays(13), amigosLaura);

        // R-004 · Externa (Booking) · Confirmada; Booking ya cobró todo
        ArrayList<Ocupante> parejaValentina = ocupantes(
                new Ocupante("Valentina Ríos Arango", LocalDate.of(1999, 4, 2)),
                new Ocupante("Tomás Herrera", LocalDate.of(1998, 10, 19)));
        Reserva r4 = externa(a, "R-004", valentina, "APT-102", hoy.plusDays(5), hoy.plusDays(8), parejaValentina,
                "BK-558201", "Booking");
        pagar(r4, MedioPago.TRANSFERENCIA, r4.getValorEstancia(), "Pago recibido de Booking");

        // R-005 · Directa · Finalizada (salió hoy) y pagada; APT-103 queda en preparación
        ArrayList<Ocupante> familiaJorge = ocupantes(
                new Ocupante("Jorge Iván Londoño", LocalDate.of(1979, 6, 8)),
                new Ocupante("Gloria Londoño", LocalDate.of(1981, 1, 17)));
        Reserva r5 = directa(a, "R-005", jorge, "APT-103", hoy, hoy.plusDays(1), familiaJorge);
        pagar(r5, MedioPago.TARJETA, r5.getValorEstancia(), "Pago total");
        a.confirmarReserva("R-005");
        a.hacerCheckIn("R-005", hoy.atTime(Alojamiento.HORA_ENTRADA));
        a.hacerCheckOut("R-005");

        // R-006 · Portal · Cancelada con más de 7 días: se devolvió todo el anticipo
        ArrayList<Ocupante> amigasMariana = ocupantes(
                new Ocupante("Mariana Castaño Vélez", LocalDate.of(2000, 3, 27)),
                new Ocupante("Daniela Vélez", LocalDate.of(2000, 11, 11)),
                new Ocupante("Juliana Mora", LocalDate.of(2001, 6, 2)));
        ReservaPortal r6 = portal(a, "R-006", mariana, "APT-203", hoy.plusDays(20), hoy.plusDays(23), amigasMariana);
        pagar(r6, MedioPago.TARJETA, r6.calcularAnticipo(), "Anticipo 30 %");
        a.confirmarReserva("R-006");
        a.cancelarReserva("R-006", hoy.atStartOfDay());

        // R-007 · Portal · No-show: el titular no llegó; se retuvo el anticipo
        ReservaPortal r7 = portal(a, "R-007", mariana, "APT-301", hoy, hoy.plusDays(2), ocupantes(
                new Ocupante("Mariana Castaño Vélez", LocalDate.of(2000, 3, 27)),
                new Ocupante("Daniel Vélez", LocalDate.of(2000, 11, 11))));
        pagar(r7, MedioPago.TARJETA, r7.calcularAnticipo(), "Anticipo 30 %");
        a.confirmarReserva("R-007");
        a.declararNoShow("R-007", hoy.atTime(Alojamiento.HORA_LIMITE_LLEGADA).plusMinutes(30));

        // R-008 · Directa · Confirmada con dos pagos, un cargo adicional y una corrección
        ArrayList<Ocupante> familiaAndres = ocupantes(
                new Ocupante("Andrés Felipe Gómez", LocalDate.of(1993, 2, 5)),
                new Ocupante("Paola Ríos", LocalDate.of(1994, 9, 12)),
                new Ocupante("Martín Gómez", LocalDate.of(2015, 4, 20)),
                new Ocupante("Emilia Gómez", LocalDate.of(2023, 1, 8)));
        Reserva r8 = directa(a, "R-008", andres, "APT-203", hoy.plusDays(15), hoy.plusDays(18), familiaAndres);
        pagar(r8, MedioPago.EFECTIVO, 300_000, "Abono inicial");
        Pago errado = new Pago(MedioPago.TRANSFERENCIA, 50_000, "Transferencia mal digitada", false);
        r8.getFolio().registrarPago(errado);
        r8.getFolio().corregir(errado);
        pagar(r8, MedioPago.TRANSFERENCIA, 500_000, "Transferencia");
        r8.getFolio().agregarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL, 45_000, "Lavandería", false));
        a.confirmarReserva("R-008");

        // R-009 · Externa (Airbnb) · Confirmada
        Reserva r9 = externa(a, "R-009", jorge, "APT-301", hoy.plusDays(10), hoy.plusDays(12), familiaJorge,
                "AB-77341", "Airbnb");
        pagar(r9, MedioPago.TRANSFERENCIA, r9.getValorEstancia(), "Pago recibido de Airbnb");

        // R-010 · Directa · Pendiente, para probar el vencimiento a 24 horas y la confirmación en recepción
        directa(a, "R-010", carlos, "APT-101", hoy.plusDays(12), hoy.plusDays(14), parejaCarlos);

        // Conflicto de canal: Booking envía otra reserva sobre las noches de R-001; se rechaza y queda registrada
        try {
            externa(a, "R-099", valentina, "APT-101", hoy.plusDays(6), hoy.plusDays(7), parejaValentina,
                    "BK-558999", "Booking");
        } catch (ReglaNegocioException esperado) {
            // Es lo esperado: así el front tiene un conflicto de ejemplo para mostrar
        }
    }

    private static void cargarNovedadesYBloqueos(Alojamiento a, LocalDate hoy) {
        Recepcionista julian = (Recepcionista) a.buscarEmpleado("1094000002");
        Recepcionista camila = (Recepcionista) a.buscarEmpleado("1094000005");
        a.buscarApartamento("APT-202").registrarNovedad(new Novedad(TipoNovedad.DANIO,
                "Fuga de agua en el baño principal", julian));
        a.buscarApartamento("APT-202").registrarNovedad(new Novedad(TipoNovedad.DANIO,
                "Humedad en la pared del baño", camila));
        a.buscarApartamento("APT-101").registrarNovedad(new Novedad(TipoNovedad.FALTANTE,
                "Faltan dos toallas de baño", julian));
        a.buscarApartamento("APT-103").registrarNovedad(new Novedad(TipoNovedad.OTRA,
                "El huésped dejó una chaqueta en el clóset", camila));

        a.registrarBloqueo("APT-301", new Bloqueo(hoy.plusDays(25), hoy.plusDays(28), "Fumigación programada"));
        a.registrarBloqueo("APT-203", new Bloqueo(hoy.plusDays(40), hoy.plusDays(45), "Remodelación de la cocina"));
    }

    // ------------------------------------------------------------------
    // Ayudantes
    // ------------------------------------------------------------------

    private static ReservaPortal portal(Alojamiento a, String codigo, Huesped titular, String apto, LocalDate entrada,
                                        LocalDate salida, ArrayList<Ocupante> grupo) {
        ReservaPortal reserva = new ReservaPortal(codigo, titular, a.buscarApartamento(apto), entrada, salida, grupo,
                a.cotizar(apto, entrada, salida, grupo), a.getPoliticaCancelacion());
        a.crearReserva(reserva);
        return reserva;
    }

    private static Reserva directa(Alojamiento a, String codigo, Huesped titular, String apto, LocalDate entrada,
                                   LocalDate salida, ArrayList<Ocupante> grupo) {
        Reserva reserva = new ReservaDirecta(codigo, titular, a.buscarApartamento(apto), entrada, salida, grupo,
                a.cotizar(apto, entrada, salida, grupo), a.getPoliticaCancelacion());
        a.crearReserva(reserva);
        return reserva;
    }

    private static Reserva externa(Alojamiento a, String codigo, Huesped titular, String apto, LocalDate entrada,
                                   LocalDate salida, ArrayList<Ocupante> grupo, String codigoExterno,
                                   String plataforma) {
        ReservaExterna reserva = new ReservaExterna(codigo, titular, a.buscarApartamento(apto), entrada, salida,
                grupo, a.cotizar(apto, entrada, salida, grupo), a.getPoliticaCancelacion(), codigoExterno,
                plataforma);
        a.recibirReservaExterna(reserva);
        return reserva;
    }

    private static void pagar(Reserva reserva, MedioPago medio, long valor, String descripcion) {
        reserva.getFolio().registrarPago(new Pago(medio, valor, descripcion, false));
    }

    private static ArrayList<Ocupante> ocupantes(Ocupante... lista) {
        return new ArrayList<>(List.of(lista));
    }
}
