import { Injectable, computed, signal } from '@angular/core';

/** Lleva la cuenta de las peticiones HTTP en curso. */
@Injectable({ providedIn: 'root' })
export class CargaService {
  private readonly pendientes = signal(0);

  /** true mientras haya al menos una petición sin terminar. */
  readonly cargando = computed(() => this.pendientes() > 0);

  iniciar(): void {
    this.pendientes.update((n) => n + 1);
  }

  terminar(): void {
    this.pendientes.update((n) => Math.max(0, n - 1));
  }
}