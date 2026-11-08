import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { catchError, of } from 'rxjs';
import { ReservaApi } from '../../../core/api/reserva.api';
import { EstadoReservaChip } from '../../../shared/components/estado-reserva/estado-reserva';
import { EstadoReserva, Reserva } from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';

/** Estados en los que la reserva todavía está "viva". */
const ACTIVAS: EstadoReserva[] = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO'];

@Component({
  selector: 'app-mis-reservas',
  imports: [
    NgTemplateOutlet,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    EstadoReservaChip,
    CopPipe,
  ],
  templateUrl: './mis-reservas.html',
  styleUrl: './mis-reservas.scss',
})
export class MisReservas {
  private readonly reservaApi = inject(ReservaApi);

  /** undefined = cargando. */
  private readonly reservas = toSignal(
    this.reservaApi.misReservas().pipe(catchError(() => of<Reserva[]>([]))),
  );

  protected readonly cargando = computed(() => this.reservas() === undefined);
  protected readonly activas = computed(() =>
    (this.reservas() ?? []).filter((r) => ACTIVAS.includes(r.estado)),
  );
  protected readonly historial = computed(() =>
    (this.reservas() ?? []).filter((r) => !ACTIVAS.includes(r.estado)),
  );
}