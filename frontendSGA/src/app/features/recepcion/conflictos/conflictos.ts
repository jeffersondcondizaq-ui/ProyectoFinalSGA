import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { ReservaApi } from '../../../core/api/reserva.api';

/** Reservas externas rechazadas por chocar con una reserva vigente (nunca la reemplazan). */
@Component({
  selector: 'app-conflictos',
  imports: [MatCardModule, MatTableModule],
  templateUrl: './conflictos.html',
  styleUrl: './conflictos.scss',
})
export class Conflictos {
  private readonly reservaApi = inject(ReservaApi);

  protected readonly conflictos = toSignal(this.reservaApi.conflictos());
  protected readonly columnas = ['fecha', 'codigoExterno', 'reserva', 'descripcion'];

  protected fecha(fechaHora: string): string {
    return new Date(fechaHora).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }
}