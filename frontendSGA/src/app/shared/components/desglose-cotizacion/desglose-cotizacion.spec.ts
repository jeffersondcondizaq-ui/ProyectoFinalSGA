import { TestBed } from '@angular/core/testing';
import { Cotizacion } from '../../models';
import { DesgloseCotizacion } from './desglose-cotizacion';

const COTIZACION: Cotizacion = {
  codigoApartamento: 'APT-101',
  fechaEntrada: '2026-11-13',
  fechaSalida: '2026-11-15',
  detalleNoches: [
    {
      fecha: '2026-11-13',
      temporada: 'BAJA',
      tarifaPersona: 80000,
      pagadores: 2,
      subtotal: 160000,
    },
    {
      fecha: '2026-11-14',
      temporada: 'MEDIA',
      tarifaPersona: 100000,
      pagadores: 2,
      subtotal: 200000,
    },
  ],
  valorTotal: 360000,
  anticipo: 108000,
};

describe('DesgloseCotizacion', () => {
  it('pinta una fila por noche, el total y el anticipo', async () => {
    const fixture = TestBed.createComponent(DesgloseCotizacion);
    fixture.componentRef.setInput('cotizacion', COTIZACION);
    await fixture.whenStable();

    const html: HTMLElement = fixture.nativeElement;
    expect(html.querySelectorAll('tr.mat-mdc-row').length).toBe(2);
    expect(html.textContent).toContain('Total (2 noches)');
    expect(html.textContent).toContain('360.000');
    expect(html.textContent).toContain('108.000');
    expect(html.querySelector('.temporada.MEDIA')?.textContent?.trim()).toBe('Media');
  });
});
