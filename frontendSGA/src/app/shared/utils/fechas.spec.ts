import { aFechaIso, desdeFechaIso } from './fechas';

describe('fechas', () => {
  it('aFechaIso rellena mes y día con cero', () => {
    expect(aFechaIso(new Date(2026, 2, 5))).toBe('2026-03-05');
  });

  it('desdeFechaIso no corre el día por la zona horaria', () => {
    const fecha = desdeFechaIso('2026-03-10');
    expect(fecha.getDate()).toBe(10);
    expect(fecha.getMonth()).toBe(2);
  });
});
