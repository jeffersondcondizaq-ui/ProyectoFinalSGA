import { Pipe, PipeTransform } from '@angular/core';

const formato = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

/** Muestra un valor en pesos colombianos sin decimales: {{ 250000 | cop }} → $ 250.000 */
@Pipe({ name: 'cop' })
export class CopPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return valor == null ? '' : formato.format(valor);
  }
}
