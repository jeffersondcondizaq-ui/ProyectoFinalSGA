import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgTemplateOutlet } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, finalize } from 'rxjs';
import { ReservaApi } from '../../../core/api/reserva.api';
import { EstadoReservaChip } from '../../../shared/components/estado-reserva/estado-reserva';
import { Reserva } from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';
import { aFechaIso } from '../../../shared/utils/fechas';

type Accion = 'confirmar' | 'checkIn' | 'checkOut' | 'noShow';

const MENSAJE: Record<Accion, string> = {
  confirmar: 'Reserva confirmada',
  checkIn: 'Check-in registrado',
  checkOut: 'Check-out registrado',
  noShow: 'No-show declarado',
};

@Component({
  selector: 'app-panel-dia',
  imports: [
    NgTemplateOutlet,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    EstadoReservaChip,
    CopPipe,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './panel-dia.html',
  styleUrl: './panel-dia.scss',
})
export class PanelDia {
  private readonly reservaApi = inject(ReservaApi);
  private readonly snackBar = inject(MatSnackBar);

  /** Día que se está viendo (por defecto hoy). */
  protected readonly fecha = new FormControl(new Date(), { nonNullable: true });
  private readonly fechaIso = toSignal(this.fecha.valueChanges, { initialValue: this.fecha.value });

  protected readonly reservas = signal<Reserva[]>([]);
  protected readonly cargando = signal(true);
  /** Código de la reserva con una acción en curso (para deshabilitar sus botones). */
  protected readonly procesando = signal<string | null>(null);
  /** El no-show es definitivo: se pide confirmar en la misma fila. */
  protected readonly confirmandoNoShow = signal<string | null>(null);

  // Las reglas (fechas, hora límite, apartamento Preparado) las valida el backend;
  // aquí solo se agrupa lo que el recepcionista necesita ver ese día.
  protected readonly llegadas = computed(() => {
    const dia = aFechaIso(this.fechaIso());
    return this.reservas().filter(
      (r) => r.fechaEntrada === dia && (r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA'),
    );
  });
  protected readonly salidas = computed(() => {
    const dia = aFechaIso(this.fechaIso());
    return this.reservas().filter((r) => r.fechaSalida === dia && r.estado === 'EN_CURSO');
  });
  protected readonly hospedados = computed(() =>
    this.reservas().filter((r) => r.estado === 'EN_CURSO'),
  );
  protected readonly pendientes = computed(() =>
    this.reservas().filter((r) => r.estado === 'PENDIENTE'),
  );

  constructor() {
    this.reservaApi
      .listar()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe((lista) => this.reservas.set(lista));
  }

  protected ejecutar(accion: Accion, reserva: Reserva): void {
    const peticiones: Record<Accion, (codigo: string) => Observable<Reserva>> = {
      confirmar: (c) => this.reservaApi.confirmar(c),
      checkIn: (c) => this.reservaApi.checkIn(c),
      checkOut: (c) => this.reservaApi.checkOut(c),
      noShow: (c) => this.reservaApi.declararNoShow(c),
    };
    this.confirmandoNoShow.set(null);
    this.procesando.set(reserva.codigo);
    peticiones[accion](reserva.codigo)
      .pipe(finalize(() => this.procesando.set(null)))
      .subscribe((actualizada) => {
        // Se reemplaza la reserva con lo que devolvió el backend (nuevo estado).
        this.reservas.update((lista) =>
          lista.map((r) => (r.codigo === actualizada.codigo ? actualizada : r)),
        );
        this.snackBar.open(`${MENSAJE[accion]} · ${actualizada.codigo}`, 'Cerrar', {
          duration: 4000,
        });
      });
  }
}
