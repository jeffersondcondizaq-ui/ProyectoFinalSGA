import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { Folio, MedioPago } from '../../models';
import { CopPipe } from '../../pipes/cop.pipe';

const MEDIO: Record<MedioPago, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
};

/** Movimientos del folio (cargos, pagos y devoluciones) con sus totales. */
@Component({
  selector: 'app-folio-movimientos',
  imports: [MatTableModule, CopPipe],
  templateUrl: './folio-movimientos.html',
  styleUrl: './folio-movimientos.scss',
})
export class FolioMovimientos {
  readonly folio = input.required<Folio>();

  protected readonly columnas = ['fecha', 'descripcion', 'cargo', 'abono'];

  protected fecha(fechaHora: string): string {
    return new Date(fechaHora).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  protected medio(medio?: MedioPago): string {
    return medio ? MEDIO[medio] : '';
  }
}