import { TestBed } from '@angular/core/testing';
import { EstadoReservaChip } from './estado-reserva';

describe('EstadoReservaChip', () => {
  it('muestra la etiqueta y la clase del estado', async () => {
    const fixture = TestBed.createComponent(EstadoReservaChip);
    fixture.componentRef.setInput('estado', 'NO_SHOW');
    await fixture.whenStable();

    const span: HTMLElement = fixture.nativeElement.querySelector('span');
    expect(span.textContent?.trim()).toBe('No-show');
    expect(span.classList).toContain('NO_SHOW');
  });
});
