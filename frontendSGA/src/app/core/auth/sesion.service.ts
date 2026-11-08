import { Injectable, computed, signal } from '@angular/core';
import { Rol, Sesion } from '../../shared/models';

const CLAVE = 'sga.sesion';

/** Guarda la sesión del usuario que inició sesión y la expone como signals. */
@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly _sesion = signal<Sesion | null>(this.leer());

  readonly sesion = this._sesion.asReadonly();
  readonly autenticado = computed(() => this._sesion() !== null);
  readonly rol = computed<Rol | null>(() => this._sesion()?.rol ?? null);
  readonly token = computed(() => this._sesion()?.token ?? null);

  iniciar(sesion: Sesion): void {
    this._sesion.set(sesion);
    try {
      localStorage.setItem(CLAVE, JSON.stringify(sesion));
    } catch {
      // Sin almacenamiento disponible: la sesión vive solo en memoria.
    }
  }

  cerrar(): void {
    this._sesion.set(null);
    try {
      localStorage.removeItem(CLAVE);
    } catch {
      // Nada que limpiar.
    }
  }

  tieneRol(...roles: Rol[]): boolean {
    const actual = this.rol();
    return actual !== null && roles.includes(actual);
  }

  private leer(): Sesion | null {
    try {
      const guardada = localStorage.getItem(CLAVE);
      return guardada ? (JSON.parse(guardada) as Sesion) : null;
    } catch {
      return null;
    }
  }
}
