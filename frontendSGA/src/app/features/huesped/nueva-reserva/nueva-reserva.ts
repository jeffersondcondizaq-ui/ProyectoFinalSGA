import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { finalize } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApartamentoApi } from '../../../core/api/apartamento.api';
import { HuespedApi } from '../../../core/api/huesped.api';
import { ReservaApi } from '../../../core/api/reserva.api';
import { DesgloseCotizacion } from '../../../shared/components/desglose-cotizacion/desglose-cotizacion';
import { Apartamento, Cotizacion, CrearReservaRequest, Ocupante } from '../../../shared/models';
import { aFechaIso, desdeFechaIso } from '../../../shared/utils/fechas';

type OcupanteForm = FormGroup<{
  nombre: FormControl<string>;
  fechaNacimiento: FormControl<Date | null>;
}>;

/** Paso del wizard en el que se muestra el resumen (se cotiza al llegar a él). */
const PASO_RESUMEN = 2;

@Component({
  selector: 'app-nueva-reserva',
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
    MatStepperModule,
    DesgloseCotizacion,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './nueva-reserva.html',
  styleUrl: './nueva-reserva.scss',
})
export class NuevaReserva implements OnInit {
  private readonly apartamentoApi = inject(ApartamentoApi);
  private readonly huespedApi = inject(HuespedApi);
  private readonly reservaApi = inject(ReservaApi);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  /** Parámetro de la ruta /huesped/nueva-reserva/:codigoApartamento */
  readonly codigoApartamento = input.required<string>();
  /** Query params que vienen de Disponibilidad. */
  readonly entrada = input<string>();
  readonly salida = input<string>();
  readonly personas = input<string>();

  protected readonly hoy = new Date();
  protected readonly usarMocks = environment.usarMocks;

  protected readonly apartamento = signal<Apartamento | null>(null);
  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly cotizando = signal(false);
  protected readonly creando = signal(false);

  /** Sin fechas no se puede reservar: hay que volver a buscar. */
  protected readonly datosCompletos = computed(() => !!this.entrada() && !!this.salida());
  protected readonly cantidadPersonas = computed(() => Math.max(1, Number(this.personas() ?? 1)));

  protected readonly ocupantes = new FormArray<OcupanteForm>([]);

  ngOnInit(): void {
    if (!this.datosCompletos()) {
      return;
    }
    this.apartamentoApi.buscar(this.codigoApartamento()).subscribe((a) => this.apartamento.set(a));

    // Fechas de nacimiento que ya se escribieron al cotizar (vienen en el state de la navegación).
    const nacimientos: string[] = history.state?.nacimientos ?? [];
    for (let i = 0; i < this.cantidadPersonas(); i++) {
      const fecha = nacimientos[i];
      this.ocupantes.push(
        new FormGroup({
          nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
          fechaNacimiento: new FormControl<Date | null>(
            fecha ? desdeFechaIso(fecha) : null,
            Validators.required,
          ),
        }),
      );
    }

    // El titular normalmente también se hospeda: se propone como primer ocupante.
    this.huespedApi.miPerfil().subscribe((perfil) => {
      const primero = this.ocupantes.at(0);
      if (!primero.controls.nombre.value) {
        primero.controls.nombre.setValue(perfil.nombre);
      }
      if (!primero.controls.fechaNacimiento.value) {
        primero.controls.fechaNacimiento.setValue(desdeFechaIso(perfil.fechaNacimiento));
      }
    });
  }

  /** Se vuelve a cotizar cada vez que se llega al resumen (los ocupantes pudieron cambiar). */
  protected cambioDePaso(evento: StepperSelectionEvent): void {
    if (evento.selectedIndex === PASO_RESUMEN) {
      this.cotizar();
    }
  }

  protected confirmar(): void {
    const body = this.solicitud();
    if (!body) {
      return;
    }
    this.creando.set(true);
    this.reservaApi
      .crearPortal(body)
      .pipe(finalize(() => this.creando.set(false)))
      .subscribe((reserva) => {
        this.snackBar.open(
          `Reserva ${reserva.codigo} creada. Queda Pendiente hasta que pagues el anticipo.`,
          'Cerrar',
          { duration: 6000 },
        );
        this.router.navigate(['/huesped/reservas', reserva.codigo]);
      });
  }

  private cotizar(): void {
    const body = this.solicitud();
    if (!body) {
      return;
    }
    this.cotizacion.set(null);
    this.cotizando.set(true);
    this.reservaApi
      .cotizar(body)
      .pipe(finalize(() => this.cotizando.set(false)))
      .subscribe((cotizacion) => this.cotizacion.set(cotizacion));
  }

  /** Datos que se envían al backend. El titular lo toma el backend del token (canal Portal). */
  private solicitud(): CrearReservaRequest | null {
    const entrada = this.entrada();
    const salida = this.salida();
    if (!entrada || !salida || this.ocupantes.invalid) {
      return null;
    }
    const ocupantes: Ocupante[] = this.ocupantes.getRawValue().map((o) => ({
      nombre: o.nombre.trim(),
      fechaNacimiento: aFechaIso(o.fechaNacimiento!),
    }));
    return {
      codigoApartamento: this.codigoApartamento(),
      fechaEntrada: entrada,
      fechaSalida: salida,
      ocupantes,
    };
  }
}