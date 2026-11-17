import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { SesionService } from '../auth/sesion.service';
import { SIN_AVISO_ERROR, errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  // Dobles de prueba: solo nos interesa saber si se llamaron y con qué.
  const snackBar = { open: vi.fn() };
  const router = { navigate: vi.fn() };
  const sesion = { cerrar: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBar },
        { provide: Router, useValue: router },
        { provide: SesionService, useValue: sesion },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('muestra el mensaje que envía el backend', () => {
    http.get('/api/reservas').subscribe({ error: () => {} });
    backend
      .expectOne('/api/reservas')
      .flush(
        { mensaje: 'El apartamento ya está reservado' },
        { status: 409, statusText: 'Conflict' },
      );
    expect(snackBar.open).toHaveBeenCalledWith('El apartamento ya está reservado', 'Cerrar', {
      duration: 5000,
    });
  });

  it('no muestra aviso si la petición lleva SIN_AVISO_ERROR', () => {
    const context = new HttpContext().set(SIN_AVISO_ERROR, true);
    http.get('/api/huespedes/123', { context }).subscribe({ error: () => {} });
    backend.expectOne('/api/huespedes/123').flush(null, { status: 404, statusText: 'Not Found' });
    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('con 401 cierra la sesión y va al login', () => {
    http.get('/api/reservas/mias').subscribe({ error: () => {} });
    backend
      .expectOne('/api/reservas/mias')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(sesion.cerrar).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
