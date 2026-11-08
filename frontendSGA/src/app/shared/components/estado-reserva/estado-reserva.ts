import { Component, computed, input } from '@angular/core';
import { EstadoReserva } from '../../models';

const ETIQUETA: Record<EstadoReserva, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  NO_SHOW: 'No-show',
};

/** Etiqueta de color con el estado de una reserva. */
@Component({
  selector: 'app-estado-reserva',
  template: `<span class="estado" [class]="estado()">{{ etiqueta() }}</span>`,
  styles: `
    .estado {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font: var(--mat-sys-label-medium);
      background: var(--mat-sys-surface-container-high);
      color: var(--mat-sys-on-surface-variant);
    }
    .PENDIENTE {
      background: var(--mat-sys-tertiary-container);
      color: var(--mat-sys-on-tertiary-container);
    }
    .CONFIRMADA,
    .EN_CURSO {
      background: var(--mat-sys-primary-container);
      color: var(--mat-sys-on-primary-container);
    }
    .CANCELADA,
    .NO_SHOW {
      background: var(--mat-sys-error-container);
      color: var(--mat-sys-on-error-container);
    }
  `,
})
export class EstadoReservaChip {
  readonly estado = input.required<EstadoReserva>();
  protected readonly etiqueta = computed(() => ETIQUETA[this.estado()]);
}