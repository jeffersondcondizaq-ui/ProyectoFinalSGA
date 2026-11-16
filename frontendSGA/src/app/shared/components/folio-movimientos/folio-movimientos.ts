import { Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { Folio, MedioPago, MovimientoFolio } from '../../models';
import { CopPipe } from '../../pipes/cop.pipe';

const MEDIO: Record<MedioPago, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
};

/** Movimientos del folio (cargos, pagos y devoluciones) con sus totales. */
@Component({
  selector: 'app-folio-movimientos',
  imports: [MatButtonModule, MatTableModule, CopPipe],
  templateUrl: './folio-movimientos.html',
  styleUrl: './folio-movimientos.scss',
})
export class FolioMovimientos {
  readonly folio = input.required<Folio>();
  readonly corregible = input(false);
  readonly corregir = output<MovimientoFolio>();

  protected readonly columnas = computed(() =>
    this.corregible()
      ? ['fecha', 'descripcion', 'cargo', 'abono', 'acciones']
      : ['fecha', 'descripcion', 'cargo', 'abono'],
  );

  protected fecha(fechaHora: string): string {
    return new Date(fechaHora).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  protected medio(medio?: MedioPago): string {
    return medio ? MEDIO[medio] : '';
  }
}
