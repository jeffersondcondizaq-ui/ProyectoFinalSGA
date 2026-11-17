import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ReservaApi } from './reserva.api';

describe('ReservaApi', () => {
  let api: ReservaApi;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(ReservaApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('disponibles envía fechas y personas como query params', () => {
    api.disponibles('2026-11-13', '2026-11-16', 3).subscribe();
    const req = backend.expectOne((r) => r.url === `${environment.apiUrl}/disponibilidad`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('entrada')).toBe('2026-11-13');
    expect(req.request.params.get('salida')).toBe('2026-11-16');
    expect(req.request.params.get('personas')).toBe('3');
    req.flush([]);
  });

  it('cotizar hace POST con los ocupantes', () => {
    const body = {
      codigoApartamento: 'APT-101',
      fechaEntrada: '2026-11-13',
      fechaSalida: '2026-11-16',
      ocupantes: [{ nombre: 'Laura', fechaNacimiento: '1995-07-18' }],
    };
    api.cotizar(body).subscribe();
    const req = backend.expectOne(`${environment.apiUrl}/cotizaciones`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('cancelar hace POST a /reservas/{codigo}/cancelar', () => {
    api.cancelar('R-001').subscribe();
    const req = backend.expectOne(`${environment.apiUrl}/reservas/R-001/cancelar`);
    expect(req.request.method).toBe('POST');
    req.flush({ totalPagado: 0, retencion: 0, devolucion: 0, regla: '' });
  });
});
