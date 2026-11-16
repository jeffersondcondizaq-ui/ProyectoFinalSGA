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
import { Observable, finalize } from 'rxjs';
import { FolioApi } from '../../../core/api/folio.api';
import { ReservaApi } from '../../../core/api/reserva.api';
import { EstadoReservaChip } from '../../../shared/components/estado-reserva/estado-reserva';
import { FolioMovimientos } from '../../../shared/components/folio-movimientos/folio-movimientos';
import {
  EstadoReserva,
  Folio as FolioModelo,
  MedioPago,
  MovimientoFolio,
  Reserva,
  TipoCargo,
} from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

/** En una reserva activa se pueden registrar pagos y cargos. */
const ACTIVAS: EstadoReserva[] = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO'];

@Component({
  selector: 'app-folio',
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
  templateUrl: './folio.html',
  styleUrl: './folio.scss',
})
export class Folio implements OnInit {
  private readonly reservaApi = inject(ReservaApi);
  private readonly folioApi = inject(FolioApi);
  private readonly snackBar = inject(MatSnackBar);

  /** Parámetro de la ruta /recepcion/folio/:codigoReserva */
  readonly codigoReserva = input.required<string>();

  protected readonly reserva = signal<Reserva | null>(null);
  protected readonly folio = signal<FolioModelo | null>(null);
  protected readonly guardando = signal(false);
  /** Movimiento que se va a corregir (se pide confirmar). */
  protected readonly aCorregir = signal<MovimientoFolio | null>(null);

  protected readonly activa = computed(() => {
    const r = this.reserva();
    return !!r && ACTIVAS.includes(r.estado);
  });

  protected readonly medios: { valor: MedioPago; etiqueta: string }[] = [
    { valor: 'EFECTIVO', etiqueta: 'Efectivo' },
    { valor: 'TARJETA', etiqueta: 'Tarjeta' },
    { valor: 'TRANSFERENCIA', etiqueta: 'Transferencia' },
  ];
  /** El cargo de alojamiento lo genera el backend al crear la reserva. */
  protected readonly tiposCargo: { valor: TipoCargo; etiqueta: string }[] = [
    { valor: 'SERVICIO_ADICIONAL', etiqueta: 'Servicio adicional' },
    { valor: 'PENALIDAD', etiqueta: 'Penalidad' },
  ];

  protected readonly pago = new FormGroup({
    medio: new FormControl<MedioPago>('EFECTIVO', { nonNullable: true }),
    valor: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    descripcion: new FormControl('', { nonNullable: true }),
  });
  protected readonly cargo = new FormGroup({
    tipo: new FormControl<TipoCargo>('SERVICIO_ADICIONAL', { nonNullable: true }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    valor: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
  });

  ngOnInit(): void {
    this.reservaApi.buscar(this.codigoReserva()).subscribe((r) => this.reserva.set(r));
    this.folioApi.obtener(this.codigoReserva()).subscribe((f) => this.folio.set(f));
  }

  protected registrarPago(): void {
    const { medio, valor, descripcion } = this.pago.getRawValue();
    if (this.pago.invalid || !valor) {
      this.pago.markAllAsTouched();
      return;
    }
    this.guardar(
      this.folioApi.registrarPago(this.codigoReserva(), {
        medio,
        valor,
        descripcion: descripcion.trim() || 'Pago en recepción',
      }),
      'Pago registrado',
      () => this.pago.reset({ medio: 'EFECTIVO', valor: null, descripcion: '' }),
    );
  }

  protected registrarCargo(): void {
    const { tipo, descripcion, valor } = this.cargo.getRawValue();
    if (this.cargo.invalid || !valor) {
      this.cargo.markAllAsTouched();
      return;
    }
    this.guardar(
      this.folioApi.registrarCargo(this.codigoReserva(), {
        tipo,
        valor,
        descripcion: descripcion.trim(),
      }),
      'Cargo registrado',
      () => this.cargo.reset({ tipo: 'SERVICIO_ADICIONAL', descripcion: '', valor: null }),
    );
  }

  protected confirmarCorreccion(): void {
    const movimiento = this.aCorregir();
    if (!movimiento) {
      return;
    }
    this.guardar(
      this.folioApi.corregir(this.codigoReserva(), movimiento.id),
      'Corrección registrada',
      () => this.aCorregir.set(null),
    );
  }

  /** Las tres operaciones devuelven el folio actualizado (saldo calculado por el backend). */
  private guardar(
    peticion: Observable<FolioModelo>,
    mensaje: string,
    alTerminar: () => void,
  ): void {
    this.guardando.set(true);
    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe((folio) => {
      this.folio.set(folio);
      alTerminar();
      this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 });
    });
  }
}
