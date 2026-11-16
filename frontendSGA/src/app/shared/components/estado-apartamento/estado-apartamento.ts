import { Component, computed, input } from '@angular/core';
import { EstadoApartamento } from '../../models';

export const ETIQUETA_ESTADO_APARTAMENTO: Record<EstadoApartamento, string> = {
  PREPARADO: 'Preparado',
  OCUPADO: 'Ocupado',
  PENDIENTE_PREPARACION: 'Pendiente de preparación',
  EN_PREPARACION: 'En preparación',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
};

/** Etiqueta de color con el estado operativo de un apartamento. */
@Component({
  selector: 'app-estado-apartamento',
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
    .PREPARADO {
      background: var(--mat-sys-primary-container);
      color: var(--mat-sys-on-primary-container);
    }
    .PENDIENTE_PREPARACION,
    .EN_PREPARACION {
      background: var(--mat-sys-tertiary-container);
      color: var(--mat-sys-on-tertiary-container);
    }
    .FUERA_DE_SERVICIO {
      background: var(--mat-sys-error-container);
      color: var(--mat-sys-on-error-container);
    }
  `,
})
export class EstadoApartamentoChip {
  readonly estado = input.required<EstadoApartamento>();
  protected readonly etiqueta = computed(() => ETIQUETA_ESTADO_APARTAMENTO[this.estado()]);
}
