import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, finalize, of, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ReservaApi } from '../../../core/api/reserva.api';
import { DesgloseCotizacion } from '../../../shared/components/desglose-cotizacion/desglose-cotizacion';
import { Apartamento, Cotizacion } from '../../../shared/models';
import { aFechaIso, desdeFechaIso } from '../../../shared/utils/fechas';

@Component({
  selector: 'app-disponibilidad',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    DesgloseCotizacion,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './disponibilidad.html',
  styleUrl: './disponibilidad.scss',
})
export class Disponibilidad {
  private readonly reservaApi = inject(ReservaApi);
  private readonly router = inject(Router);

  /** Query params de la URL (?entrada=YYYY-MM-DD&salida=YYYY-MM-DD&personas=N). */
  readonly entrada = input<string>();
  readonly salida = input<string>();
  readonly personas = input<string>();

  protected readonly hoy = new Date();
  protected readonly usarMocks = environment.usarMocks;

  protected readonly busqueda = new FormGroup({
    entrada: new FormControl<Date | null>(null, Validators.required),
    salida: new FormControl<Date | null>(null, Validators.required),
    personas: new FormControl<number>(2, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  /** Criterios de búsqueda tomados de la URL; cada vez que cambian se consulta de nuevo. */
  protected readonly criterios = computed(() => {
    const entrada = this.entrada();
    const salida = this.salida();
    if (!entrada || !salida) {
      return null;
    }
    return { entrada, salida, personas: Math.max(1, Number(this.personas() ?? 1)) };
  });

  protected readonly consultando = signal(false);

  /** null = todavía no se ha buscado; [] = no hay apartamentos disponibles. */
  protected readonly disponibles = toSignal(
    toObservable(this.criterios).pipe(
      switchMap((c) => {
        if (!c) {
          return of(null);
        }
        this.consultando.set(true);
        return this.reservaApi.disponibles(c.entrada, c.salida, c.personas).pipe(
          catchError(() => of<Apartamento[]>([])),
          finalize(() => this.consultando.set(false)),
        );
      }),
    ),
    { initialValue: null },
  );

  // --- Cotización del apartamento elegido ---
  protected readonly seleccionado = signal<Apartamento | null>(null);
  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly cotizando = signal(false);
  /** Fecha de nacimiento de cada persona: el backend decide quién paga (menores de 6 no pagan). */
  protected readonly nacimientos = new FormArray<FormControl<Date | null>>([]);

  constructor() {
    // Cuando cambian los criterios de la URL: llenar el formulario y descartar la cotización anterior.
    effect(() => {
      const c = this.criterios();
      if (c) {
        this.busqueda.setValue({
          entrada: desdeFechaIso(c.entrada),
          salida: desdeFechaIso(c.salida),
          personas: c.personas,
        });
      }
      this.seleccionado.set(null);
      this.cotizacion.set(null);
    });
  }

  protected buscar(): void {
    const { entrada, salida, personas } = this.busqueda.getRawValue();
    if (this.busqueda.invalid || !entrada || !salida) {
      this.busqueda.markAllAsTouched();
      return;
    }
    if (salida <= entrada) {
      this.busqueda.controls.salida.setErrors({ minimoUnaNoche: true });
      return;
    }
    // Se guardan en la URL: así la búsqueda se puede recargar o compartir.
    this.router.navigate(['/huesped/disponibilidad'], {
      queryParams: { entrada: aFechaIso(entrada), salida: aFechaIso(salida), personas },
    });
  }

  protected elegir(apartamento: Apartamento): void {
    const c = this.criterios();
    if (!c) {
      return;
    }
    this.seleccionado.set(apartamento);
    this.cotizacion.set(null);
    this.nacimientos.clear();
    for (let i = 0; i < c.personas; i++) {
      this.nacimientos.push(new FormControl<Date | null>(null, Validators.required));
    }
  }

  protected cotizar(): void {
    const c = this.criterios();
    const apartamento = this.seleccionado();
    if (!c || !apartamento) {
      return;
    }
    if (this.nacimientos.invalid) {
      this.nacimientos.markAllAsTouched();
      return;
    }
    this.cotizando.set(true);
    this.reservaApi
      .cotizar({
        codigoApartamento: apartamento.codigo,
        fechaEntrada: c.entrada,
        fechaSalida: c.salida,
        // Para cotizar solo importa la fecha de nacimiento; los nombres se piden al reservar.
        ocupantes: this.nacimientos.getRawValue().map((fecha, i) => ({
          nombre: `Persona ${i + 1}`,
          fechaNacimiento: aFechaIso(fecha!),
        })),
      })
      .pipe(finalize(() => this.cotizando.set(false)))
      .subscribe((cotizacion) => this.cotizacion.set(cotizacion));
  }

  protected reservar(): void {
    const c = this.criterios();
    const apartamento = this.seleccionado();
    if (!c || !apartamento) {
      return;
    }
    this.router.navigate(['/huesped/nueva-reserva', apartamento.codigo], {
      queryParams: { entrada: c.entrada, salida: c.salida, personas: c.personas },
      // Las fechas de nacimiento ya escritas se pasan al wizard para no pedirlas otra vez.
      state: { nacimientos: this.nacimientos.getRawValue().map((f) => aFechaIso(f!)) },
    });
  }
}