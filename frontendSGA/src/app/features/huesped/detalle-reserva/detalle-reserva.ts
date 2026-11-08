import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { FolioApi } from '../../../core/api/folio.api';
import { ReservaApi } from '../../../core/api/reserva.api';
import { EstadoReservaChip } from '../../../shared/components/estado-reserva/estado-reserva';
import { FolioMovimientos } from '../../../shared/components/folio-movimientos/folio-movimientos';
import {
  EstadoReserva,
  Folio,
  MedioPago,
  Reserva,
  SimulacionCancelacion,
} from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

/** Estados en los que el huésped todavía puede abonar a su folio. */
const ESTADOS_CON_PAGO: EstadoReserva[] = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO'];
/** Según el enunciado, el huésped solo cancela reservas Pendientes o Confirmadas. */
const ESTADOS_CANCELABLES: EstadoReserva[] = ['PENDIENTE', 'CONFIRMADA'];

@Component({
  selector: 'app-detalle-reserva',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    EstadoReservaChip,
    FolioMovimientos,
    CopPipe,
  ],
  templateUrl: './detalle-reserva.html',
  styleUrl: './detalle-reserva.scss',
})
export class DetalleReserva implements OnInit {
  private readonly reservaApi = inject(ReservaApi);
  private readonly folioApi = inject(FolioApi);
  private readonly snackBar = inject(MatSnackBar);

  /** Parámetro de la ruta /huesped/reservas/:codigo */
  readonly codigo = input.required<string>();

  protected readonly usarMocks = environment.usarMocks;
  protected readonly reserva = signal<Reserva | null>(null);
  protected readonly folio = signal<Folio | null>(null);

  // --- Pago ---
  protected readonly mediosPago: { valor: MedioPago; etiqueta: string }[] = [
    { valor: 'TARJETA', etiqueta: 'Tarjeta' },
    { valor: 'TRANSFERENCIA', etiqueta: 'Transferencia' },
  ];
  protected readonly pagando = signal(false);
  protected readonly mostrarPago = signal(false);
  protected readonly pago = new FormGroup({
    medio: new FormControl<MedioPago>('TARJETA', { nonNullable: true }),
    valor: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
  });

  // --- Cancelación ---
  protected readonly simulacion = signal<SimulacionCancelacion | null>(null);
  protected readonly cancelando = signal(false);

  protected readonly puedePagar = computed(() => {
    const r = this.reserva();
    const f = this.folio();
    return !!r && !!f && ESTADOS_CON_PAGO.includes(r.estado) && f.saldo > 0;
  });
  protected readonly puedeCancelar = computed(() => {
    const r = this.reserva();
    return !!r && ESTADOS_CANCELABLES.includes(r.estado);
  });
  /** Si la reserva sigue Pendiente y aún no tiene pagos, lo que corresponde es el anticipo. */
  protected readonly debeAnticipo = computed(() => {
    const r = this.reserva();
    return r?.estado === 'PENDIENTE' && this.folio()?.totalPagos === 0 && !!r.anticipo;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected abrirPago(): void {
    const sugerido = this.debeAnticipo() ? this.reserva()!.anticipo! : this.folio()!.saldo;
    this.pago.reset({ medio: 'TARJETA', valor: sugerido });
    this.pago.controls.valor.setValidators([
      Validators.required,
      Validators.min(1),
      Validators.max(this.folio()!.saldo),
    ]);
    this.pago.controls.valor.updateValueAndValidity();
    this.simulacion.set(null);
    this.mostrarPago.set(true);
  }

  protected pagar(): void {
    const { medio, valor } = this.pago.getRawValue();
    if (this.pago.invalid || !valor) {
      this.pago.markAllAsTouched();
      return;
    }
    this.pagando.set(true);
    this.folioApi
      .registrarPago(this.codigo(), {
        medio,
        valor,
        descripcion: this.debeAnticipo() ? 'Anticipo 30 %' : 'Abono',
      })
      .pipe(finalize(() => this.pagando.set(false)))
      .subscribe((folio) => {
        this.folio.set(folio);
        this.mostrarPago.set(false);
        this.snackBar.open('Pago registrado', 'Cerrar', { duration: 4000 });
        // El estado lo decide el backend (p. ej. Pendiente → Confirmada con el anticipo).
        this.reservaApi.buscar(this.codigo()).subscribe((r) => this.reserva.set(r));
      });
  }

  /** Primero se le muestra al huésped cuánto se le devolvería, sin cancelar todavía. */
  protected pedirCancelacion(): void {
    this.mostrarPago.set(false);
    this.reservaApi.simularCancelacion(this.codigo()).subscribe((s) => this.simulacion.set(s));
  }

  protected confirmarCancelacion(): void {
    this.cancelando.set(true);
    this.reservaApi
      .cancelar(this.codigo())
      .pipe(finalize(() => this.cancelando.set(false)))
      .subscribe((resultado) => {
        this.simulacion.set(null);
        this.snackBar.open(
          `Reserva cancelada. Devolución: ${new CopPipe().transform(resultado.devolucion)}`,
          'Cerrar',
          { duration: 6000 },
        );
        this.cargar();
      });
  }

  private cargar(): void {
    this.reservaApi.buscar(this.codigo()).subscribe((r) => this.reserva.set(r));
    this.folioApi.obtener(this.codigo()).subscribe((f) => this.folio.set(f));
  }
}