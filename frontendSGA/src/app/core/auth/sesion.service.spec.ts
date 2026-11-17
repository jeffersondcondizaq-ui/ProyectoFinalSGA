import { TestBed } from '@angular/core/testing';
import { Sesion } from '../../shared/models';
import { SesionService } from './sesion.service';

const LAURA: Sesion = { token: 't-1', usuario: 'laura', nombre: 'Laura', rol: 'HUESPED' };

describe('SesionService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('empieza sin sesión', () => {
    const sesion = TestBed.inject(SesionService);
    expect(sesion.autenticado()).toBe(false);
    expect(sesion.rol()).toBeNull();
    expect(sesion.token()).toBeNull();
  });

  it('iniciar guarda la sesión y expone rol y token', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciar(LAURA);
    expect(sesion.autenticado()).toBe(true);
    expect(sesion.rol()).toBe('HUESPED');
    expect(sesion.token()).toBe('t-1');
    expect(sesion.tieneRol('HUESPED')).toBe(true);
    expect(sesion.tieneRol('ADMINISTRADOR', 'RECEPCIONISTA')).toBe(false);
  });

  it('recupera la sesión guardada al recargar la página', () => {
    TestBed.inject(SesionService).iniciar(LAURA);
    TestBed.resetTestingModule(); // simula recargar: se crea un servicio nuevo
    expect(TestBed.inject(SesionService).sesion()).toEqual(LAURA);
  });

  it('cerrar borra la sesión', () => {
    const sesion = TestBed.inject(SesionService);
    sesion.iniciar(LAURA);
    sesion.cerrar();
    expect(sesion.autenticado()).toBe(false);
    expect(localStorage.getItem('sga.sesion')).toBeNull();
  });
});
