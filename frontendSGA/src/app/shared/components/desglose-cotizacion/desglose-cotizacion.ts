import { Component, computed, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { Cotizacion, TipoTemporada } from '../../models';
import { CopPipe } from '../../pipes/cop.pipe';
import { desdeFechaIso } from '../../utils/fechas';

const ETIQUETA_TEMPORADA: Record<TipoTemporada, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
};

/** Tabla del desglose noche por noche de una cotización (la calcula el backend). */
@Component({
  selector: 'app-desglose-cotizacion',
  imports: [MatTableModule, CopPipe],
  templateUrl: './desglose-cotizacion.html',
  styleUrl: './desglose-cotizacion.scss',
})
export class DesgloseCotizacion {
  readonly cotizacion = input.required<Cotizacion>();

  protected readonly columnas = ['fecha', 'temporada', 'tarifa', 'pagadores', 'subtotal'];
  protected readonly noches = computed(() => this.cotizacion().detalleNoches.length);

  protected fecha(fechaIso: string): string {
    return desdeFechaIso(fechaIso).toLocaleDateString('es-CO', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
    });
  }

  protected temporada(tipo: TipoTemporada): string {
    return ETIQUETA_TEMPORADA[tipo];
  }
}