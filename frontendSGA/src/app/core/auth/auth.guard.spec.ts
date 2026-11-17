import { TestBed } from '@angular/core/testing';
import { CanMatchFn, Route, Router, UrlTree, provideRouter } from '@angular/router';
import { SesionService } from './sesion.service';
import { rolGuard, sinSesionGuard } from './auth.guard';

/** Ejecuta un guard dentro del inyector de Angular (los guards usan inject()). */
function ejecutar(guard: CanMatchFn) {
  return TestBed.runInInjectionContext(() =>
    guard({} as Route, [], {} as Parameters<CanMatchFn>[2]),
  );
}

describe('Guards de autenticación', () => {
  let sesion: SesionService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    sesion = TestBed.inject(SesionService);
    router = TestBed.inject(Router);
  });

  it('rolGuard manda al login si no hay sesión', () => {
    const resultado = ejecutar(rolGuard('ADMINISTRADOR'));
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/login');
  });

  it('rolGuard manda al inicio si el rol no corresponde', () => {
    sesion.iniciar({ token: 't', usuario: 'laura', nombre: 'Laura', rol: 'HUESPED' });
    const resultado = ejecutar(rolGuard('ADMINISTRADOR'));
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/inicio');
  });

  it('rolGuard deja pasar al rol correcto', () => {
    sesion.iniciar({ token: 't', usuario: 'admin', nombre: 'Diana', rol: 'ADMINISTRADOR' });
    expect(ejecutar(rolGuard('ADMINISTRADOR'))).toBe(true);
  });

  it('sinSesionGuard no deja abrir el login con sesión iniciada', () => {
    expect(ejecutar(sinSesionGuard)).toBe(true);
    sesion.iniciar({ token: 't', usuario: 'admin', nombre: 'Diana', rol: 'ADMINISTRADOR' });
    expect(router.serializeUrl(ejecutar(sinSesionGuard) as UrlTree)).toBe('/inicio');
  });
});
