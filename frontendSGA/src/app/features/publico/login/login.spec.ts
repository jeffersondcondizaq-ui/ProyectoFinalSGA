import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AutenticacionApi } from '../../../core/api/autenticacion.api';
import { SesionService } from '../../../core/auth/sesion.service';
import { Login } from './login';

describe('Login', () => {
  // El API se reemplaza por un doble: la prueba no hace peticiones reales.
  const auth = {
    login: vi.fn(() =>
      of({ token: 't', usuario: 'admin', nombre: 'Diana', rol: 'ADMINISTRADOR' as const }),
    ),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), { provide: AutenticacionApi, useValue: auth }],
    });
  });

  it('no llama al API si el formulario está vacío', () => {
    const login = TestBed.createComponent(Login).componentInstance;
    login['ingresar']();
    expect(auth.login).not.toHaveBeenCalled();
    expect(login['form'].touched).toBe(true);
  });

  it('inicia sesión y lleva al inicio de su rol', () => {
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const login = TestBed.createComponent(Login).componentInstance;

    login['form'].setValue({ usuario: 'admin', contrasena: 'secreta' });
    login['ingresar']();

    expect(auth.login).toHaveBeenCalledWith({ usuario: 'admin', contrasena: 'secreta' });
    expect(TestBed.inject(SesionService).rol()).toBe('ADMINISTRADOR');
    expect(navegar).toHaveBeenCalledWith('/admin');
  });
});
