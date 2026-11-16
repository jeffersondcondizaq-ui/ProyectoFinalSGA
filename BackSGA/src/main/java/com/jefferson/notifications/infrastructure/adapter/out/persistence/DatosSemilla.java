package com.jefferson.notifications.infrastructure.adapter.out.persistence;

import com.jefferson.notifications.domain.exception.ReglaNegocioException;
import com.jefferson.notifications.domain.model.Alojamiento;
import com.jefferson.notifications.domain.model.apartamento.Apartamento;
import com.jefferson.notifications.domain.model.apartamento.EstadoApartamento;
import com.jefferson.notifications.domain.model.apartamento.TipoTemporada;
import com.jefferson.notifications.domain.model.configuracion.PeriodoTemporada;
import com.jefferson.notifications.domain.model.folio.MedioPago;
import com.jefferson.notifications.domain.model.folio.Pago;
import com.jefferson.notifications.domain.model.persona.Administrador;
import com.jefferson.notifications.domain.model.persona.Huesped;
import com.jefferson.notifications.domain.model.persona.Ocupante;
import com.jefferson.notifications.domain.model.persona.Recepcionista;
import com.jefferson.notifications.domain.model.reserva.ReservaDirecta;
import com.jefferson.notifications.domain.model.reserva.ReservaExterna;
import com.jefferson.notifications.domain.model.reserva.ReservaPortal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de ejemplo con los que arranca el back
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
        cargarReservas(alojamiento);
        // Estados operativos al final, para no estorbar a las reservas de ejemplo
        alojamiento.buscarApartamento("APT-201").cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, null);
        alojamiento.buscarApartamento("APT-202").cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO,
                "Daño en la tubería del baño principal");
        return alojamiento;
    }

    // ------------------------------------------------------------------
    // Personas
    // ------------------------------------------------------------------

    private static void cargarEmpleados(Alojamiento alojamiento) {
        alojamiento.contratarEmpleado(new Administrador("Diana Marcela Ospina", LocalDate.of(1985, 4, 12),
                "1094000001", "3104567890", "diana.ospina@apartasuites.co", "admin", "admin123",
                LocalDate.of(2022, 2, 1)));
        alojamiento.contratarEmpleado(new Recepcionista("Julián Andrés Mejía", LocalDate.of(1998, 9, 30),
                "1094000002", "3112223344", "julian.mejia@apartasuites.co", "recepcion", "recepcion123",
                LocalDate.of(2024, 6, 15)));
        alojamiento.contratarEmpleado(new Recepcionista("Paula Andrea Cárdenas", LocalDate.of(1996, 1, 22),
                "1094000003", "3159876543", "paula.cardenas@apartasuites.co", "pcardenas", "paula123",
                LocalDate.of(2023, 3, 1)));
        alojamiento.desvincularEmpleado("1094000003");
    }

    private static void cargarHuespedes(Alojamiento alojamiento) {
        alojamiento.registrarHuesped(new Huesped("Laura Gómez Restrepo", LocalDate.of(1995, 7, 18),
                "1094123456", "3201234567", "laura.gomez@correo.com", "laura", "laura123"));
        alojamiento.registrarHuesped(new Huesped("Carlos Ruiz Patiño", LocalDate.of(1988, 11, 23),
                "1053998877", "3157654321", "carlos.ruiz@correo.com", "1053998877", "carlos123"));
        alojamiento.registrarHuesped(new Huesped("Valentina Ríos Arango", LocalDate.of(1999, 4, 2),
                "1010456789", "3009876543", "valentina.rios@correo.com", "1010456789", "valentina123"));
    }

    // ------------------------------------------------------------------
    // Apartamentos y temporadas
    // ------------------------------------------------------------------

    private static void cargarApartamentos(Alojamiento alojamiento) {
        registrar(alojamiento, "APT-101", "Suite Cocora", 2, 4, 80_000, 100_000, 130_000,
                "Balcón", "Vista a las montañas", "Chimenea");
        registrar(alojamiento, "APT-102", "Loft Palma de Cera", 1, 2, 70_000, 90_000, 115_000,
                "Jacuzzi", "Vista al valle");
        registrar(alojamiento, "APT-201", "Casa del Valle", 3, 6, 65_000, 90_000, 120_000,
                "Balcón", "Cocina amplia", "Zona de BBQ");
        registrar(alojamiento, "APT-202", "Mirador del Quindío", 2, 3, 75_000, 95_000, 125_000,
                "Vista a las montañas", "Terraza privada");
    }

    private static void registrar(Alojamiento alojamiento, String codigo, String nombre, int dormitorios,
                                  int capacidad, long baja, long media, long alta, String... caracteristicas) {
        Apartamento apartamento = new Apartamento(codigo, nombre, dormitorios, capacidad,
                new ArrayList<>(List.of(caracteristicas)), EstadoApartamento.PREPARADO, null, true,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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
    // Reservas de ejemplo (una por cada situación que muestra el front)
    // ------------------------------------------------------------------

    private static void cargarReservas(Alojamiento alojamiento) {
        LocalDate hoy = LocalDate.now();
        Huesped laura = alojamiento.buscarHuesped("1094123456");
        Huesped carlos = alojamiento.buscarHuesped("1053998877");
        Huesped valentina = alojamiento.buscarHuesped("1010456789");

        // R-001: portal, Confirmada con el anticipo del 30 % pagado (la niña de 4 años no paga)
        ArrayList<Ocupante> familiaLaura = ocupantes(
                new Ocupante("Laura Gómez Restrepo", LocalDate.of(1995, 7, 18)),
                new Ocupante("Andrés Felipe Gómez", LocalDate.of(1993, 2, 5)),
                new Ocupante("Sofía Gómez", LocalDate.of(2022, 3, 10)));
        ReservaPortal r1 = new ReservaPortal("R-001", laura, alojamiento.buscarApartamento("APT-101"),
                hoy.plusDays(5), hoy.plusDays(8), familiaLaura,
                alojamiento.cotizar("APT-101", hoy.plusDays(5), hoy.plusDays(8), familiaLaura),
                alojamiento.getPoliticaCancelacion());
        alojamiento.crearReserva(r1);
        r1.getFolio().registrarPago(new Pago(MedioPago.TARJETA, r1.calcularAnticipo(), "Anticipo 30 %", false));
        alojamiento.confirmarReserva("R-001");

        // R-002: directa, En curso (llegó hoy) con un pago parcial; APT-102 queda Ocupado
        ArrayList<Ocupante> parejaCarlos = ocupantes(
                new Ocupante("Carlos Ruiz Patiño", LocalDate.of(1988, 11, 23)),
                new Ocupante("Mónica Salazar", LocalDate.of(1990, 5, 14)));
        ReservaDirecta r2 = new ReservaDirecta("R-002", carlos, alojamiento.buscarApartamento("APT-102"),
                hoy, hoy.plusDays(3), parejaCarlos,
                alojamiento.cotizar("APT-102", hoy, hoy.plusDays(3), parejaCarlos),
                alojamiento.getPoliticaCancelacion());
        alojamiento.crearReserva(r2);
        r2.getFolio().registrarPago(new Pago(MedioPago.EFECTIVO, 200_000, "Abono en recepción", false));
        alojamiento.confirmarReserva("R-002");
        alojamiento.hacerCheckIn("R-002", hoy.atTime(Alojamiento.HORA_ENTRADA));

        // R-003: portal, Pendiente (sin anticipo todavía)
        ArrayList<Ocupante> amigosLaura = ocupantes(
                new Ocupante("Laura Gómez Restrepo", LocalDate.of(1995, 7, 18)),
                new Ocupante("Andrés Felipe Gómez", LocalDate.of(1993, 2, 5)),
                new Ocupante("Camilo Restrepo", LocalDate.of(1996, 8, 30)),
                new Ocupante("Natalia Pérez", LocalDate.of(1997, 12, 1)));
        alojamiento.crearReserva(new ReservaPortal("R-003", laura, alojamiento.buscarApartamento("APT-201"),
                hoy.plusDays(10), hoy.plusDays(13), amigosLaura,
                alojamiento.cotizar("APT-201", hoy.plusDays(10), hoy.plusDays(13), amigosLaura),
                alojamiento.getPoliticaCancelacion()));

        // R-004: externa de Booking, queda Confirmada; Booking ya le cobró todo al huésped
        ArrayList<Ocupante> parejaValentina = ocupantes(
                new Ocupante("Valentina Ríos Arango", LocalDate.of(1999, 4, 2)),
                new Ocupante("Tomás Herrera", LocalDate.of(1998, 10, 19)));
        ReservaExterna r4 = new ReservaExterna("R-004", valentina,
                alojamiento.buscarApartamento("APT-102"), hoy.plusDays(5), hoy.plusDays(8), parejaValentina,
                alojamiento.cotizar("APT-102", hoy.plusDays(5), hoy.plusDays(8), parejaValentina),
                alojamiento.getPoliticaCancelacion(), "BK-558201", "Booking");
        alojamiento.recibirReservaExterna(r4);
        r4.getFolio().registrarPago(new Pago(MedioPago.TRANSFERENCIA, r4.getValorEstancia(),
                "Pago recibido de Booking", false));

        // Conflicto de canal: Booking envía otra reserva sobre las noches de R-001; se rechaza y queda registrada
        try {
            alojamiento.recibirReservaExterna(new ReservaExterna("R-005", valentina,
                    alojamiento.buscarApartamento("APT-101"), hoy.plusDays(6), hoy.plusDays(7), parejaValentina,
                    alojamiento.cotizar("APT-101", hoy.plusDays(6), hoy.plusDays(7), parejaValentina),
                    alojamiento.getPoliticaCancelacion(), "BK-558999", "Booking"));
        } catch (ReglaNegocioException esperado) {
            // Es lo esperado: así el front tiene un conflicto de ejemplo para mostrar
        }
    }

    private static ArrayList<Ocupante> ocupantes(Ocupante... lista) {
        return new ArrayList<>(List.of(lista));
    }
}