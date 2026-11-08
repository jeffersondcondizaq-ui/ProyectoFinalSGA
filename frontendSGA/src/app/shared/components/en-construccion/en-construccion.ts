import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

/** Marcador para las pantallas del cascarón que todavía no se implementan. */
@Component({
  selector: 'app-en-construccion',
  imports: [MatCardModule, MatIconModule],
  template: `
    <h1 class="titulo">{{ titulo() }}</h1>
    <mat-card appearance="outlined">
      <mat-card-content class="contenido">
        <mat-icon>construction</mat-icon>
        <div>
          <p>{{ descripcion() }}</p>
          @if (tarjeta()) {
            <p class="tarjeta">Tarjeta de Trello: {{ tarjeta() }}</p>
          }
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: `
    .titulo {
      font: var(--mat-sys-headline-small);
      margin: 0 0 16px;
    }
    .contenido {
      display: flex;
      gap: 16px;
      align-items: flex-start;
    }
    .tarjeta {
      color: var(--mat-sys-on-surface-variant);
      font: var(--mat-sys-body-small);
    }
  `,
})
export class EnConstruccion {
  readonly titulo = input.required<string>();
  readonly descripcion = input('');
  readonly tarjeta = input('');
}
