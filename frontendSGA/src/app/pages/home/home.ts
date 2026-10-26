import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HealthService } from '../../core/health.service';

@Component({
  selector: 'app-home',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './home.html',
})
export class Home {
  private readonly health = inject(HealthService);

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
