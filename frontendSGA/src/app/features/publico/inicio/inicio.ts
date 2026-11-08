import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HealthApi } from '../../../core/api/health.api';

@Component({
  selector: 'app-inicio',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './inicio.html',
})
export class Inicio {
  private readonly health = inject(HealthApi);

  protected readonly status = signal<string>('Consultando...');

  constructor() {
    this.refresh();
  }

  protected refresh(): void {
    this.status.set('Consultando...');
    this.health.check().subscribe({
      next: (res) => this.status.set(res.status),
      error: (err) => this.status.set(`Error (${err.status || 'sin conexión'})`),
    });
  }
}
