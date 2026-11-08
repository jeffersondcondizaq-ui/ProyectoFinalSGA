import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, map, of } from 'rxjs';
import { ApartamentoApi } from '../../../core/api/apartamento.api';
import { HealthApi } from '../../../core/api/health.api';
import { SesionService } from '../../../core/auth/sesion.service';
import { Apartamento } from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';
import { aFechaIso } from '../../../shared/utils/fechas';

@Component({
  selector: 'app-inicio',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    CopPipe,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio {
  private readonly apartamentoApi = inject(ApartamentoApi);
  private readonly health = inject(HealthApi);
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  /** No se puede reservar con fecha de entrada anterior a hoy. */
  protected readonly hoy = new Date();

  /** Apartamentos que se pueden vender (activos y no fuera de servicio). */
  protected readonly apartamentos = toSignal(
    this.apartamentoApi.listar().pipe(
      map((lista) => lista.filter((a) => a.activo && a.estado !== 'FUERA_DE_SERVICIO')),
      catchError(() => of<Apartamento[]>([])),
    ),
    { initialValue: [] },
  );

  protected readonly capacidadMaxima = computed(() =>
    Math.max(1, ...this.apartamentos().map((a) => a.capacidadMaxima)),
  );

  protected readonly busqueda = new FormGroup({
    entrada: new FormControl<Date | null>(null, Validators.required),
    salida: new FormControl<Date | null>(null, Validators.required),
    personas: new FormControl<number>(2, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected readonly estadoBackend = signal('Consultando...');

  constructor() {
    this.consultarBackend();
  }

  /** Tarifa más baja del apartamento (temporada Baja), por persona por noche. */
  protected tarifaDesde(apartamento: Apartamento): number {
    return Math.min(...apartamento.tarifas.map((t) => t.valorPersonaNoche));
  }

  protected buscar(): void {
    const { entrada, salida, personas } = this.busqueda.getRawValue();
    if (this.busqueda.invalid || !entrada || !salida) {
      this.busqueda.markAllAsTouched();
      return;
    }
    if (salida <= entrada) {
      // Mínimo una noche: la salida debe ser posterior a la entrada.
      this.busqueda.controls.salida.setErrors({ minimoUnaNoche: true });
      return;
    }

    const queryParams = { entrada: aFechaIso(entrada), salida: aFechaIso(salida), personas };
    if (this.sesion.tieneRol('HUESPED')) {
      this.router.navigate(['/huesped/disponibilidad'], { queryParams });
    } else {
      // Para reservar hay que iniciar sesión como huésped.
      this.router.navigate(['/login'], { queryParams });
    }
  }

  protected consultarBackend(): void {
    this.estadoBackend.set('Consultando...');
    this.health.check().subscribe({
      next: (res) => this.estadoBackend.set(res.status),
      error: (err) => this.estadoBackend.set(`Error (${err.status || 'sin conexión'})`),
    });
  }
}
