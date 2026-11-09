import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { finalize } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { HuespedApi } from '../../../core/api/huesped.api';
import { ReservaApi } from '../../../core/api/reserva.api';
import { DesgloseCotizacion } from '../../../shared/components/desglose-cotizacion/desglose-cotizacion';
import {
  OcupanteForm,
  OcupantesForm,
  aOcupantes,
  crearOcupante,
} from '../../../shared/components/ocupantes-form/ocupantes-form';
import { Apartamento, Cotizacion, CrearReservaRequest, Huesped } from '../../../shared/models';
import { aFechaIso, desdeFechaIso } from '../../../shared/utils/fechas';

const PASO_OCUPANTES = 2;
const PASO_RESUMEN = 3;

@Component({
  selector: 'app-reserva-directa',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatRadioModule,
    MatStepperModule,
    DesgloseCotizacion,
    OcupantesForm,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './reserva-directa.html',
  styleUrl: './reserva-directa.scss',
})
export class ReservaDirecta {
  private readonly huespedApi = inject(HuespedApi);
  private readonly reservaApi = inject(ReservaApi);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly hoy = new Date();
  protected readonly usarMocks = environment.usarMocks;

  // --- Paso 1: titular ---
  protected readonly documento = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/^\d{6,10}$/)],
  });
  protected readonly buscandoHuesped = signal(false);
  /** true cuando el documento no existe y hay que registrar al huésped. */
  protected readonly registrarNuevo = signal(false);
  protected readonly guardandoHuesped = signal(false);
  /** Control oculto del paso 1: el stepper no avanza hasta tener titular. */
  protected readonly titular = new FormControl<Huesped | null>(null, Validators.required);
  protected readonly nuevoHuesped = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    fechaNacimiento: new FormControl<Date | null>(null, Validators.required),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{10}$/)],
    }),
    correo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  // --- Paso 2: estadía y apartamento ---
  protected readonly estadia = new FormGroup({
    entrada: new FormControl<Date | null>(null, Validators.required),
    salida: new FormControl<Date | null>(null, Validators.required),
    personas: new FormControl<number>(2, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
    apartamento: new FormControl<string | null>(null, Validators.required),
  });
  protected readonly disponibles = signal<Apartamento[] | null>(null);
  protected readonly consultando = signal(false);

  // --- Paso 3 y 4 ---
  protected readonly ocupantes = new FormArray<OcupanteForm>([]);
  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly cotizando = signal(false);
  protected readonly creando = signal(false);

  protected buscarHuesped(): void {
    if (this.documento.invalid) {
      this.documento.markAsTouched();
      return;
    }
    this.titular.setValue(null);
    this.registrarNuevo.set(false);
    this.buscandoHuesped.set(true);
    this.huespedApi
      .buscar(this.documento.value)
      .pipe(finalize(() => this.buscandoHuesped.set(false)))
      .subscribe({
        next: (huesped) => this.titular.setValue(huesped),
        error: (error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.nuevoHuesped.reset();
            this.registrarNuevo.set(true);
          } else {
            this.snackBar.open('No se pudo buscar el huésped', 'Cerrar', { duration: 4000 });
          }
        },
      });
  }

  protected guardarHuesped(): void {
    const { fechaNacimiento, ...datos } = this.nuevoHuesped.getRawValue();
    if (this.nuevoHuesped.invalid || !fechaNacimiento) {
      this.nuevoHuesped.markAllAsTouched();
      return;
    }
    this.guardandoHuesped.set(true);
    this.huespedApi
      .crear({
        ...datos,
        nombre: datos.nombre.trim(),
        documento: this.documento.value,
        fechaNacimiento: aFechaIso(fechaNacimiento),
      })
      .pipe(finalize(() => this.guardandoHuesped.set(false)))
      .subscribe((huesped) => {
        this.titular.setValue(huesped);
        this.registrarNuevo.set(false);
        this.snackBar.open('Huésped registrado', 'Cerrar', { duration: 3000 });
      });
  }

  protected buscarDisponibles(): void {
    const { entrada, salida, personas } = this.estadia.getRawValue();
    if (!entrada || !salida || this.estadia.controls.personas.invalid) {
      this.estadia.markAllAsTouched();
      return;
    }
    if (salida <= entrada) {
      this.estadia.controls.salida.setErrors({ minimoUnaNoche: true });
      return;
    }
    this.estadia.controls.apartamento.setValue(null);
    this.consultando.set(true);
    this.reservaApi
      .disponibles(aFechaIso(entrada), aFechaIso(salida), personas)
      .pipe(finalize(() => this.consultando.set(false)))
      .subscribe((lista) => this.disponibles.set(lista));
  }

  protected cambioDePaso(evento: StepperSelectionEvent): void {
    if (evento.selectedIndex === PASO_OCUPANTES) {
      this.prepararOcupantes();
    }
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
      .crearDirecta(body)
      .pipe(finalize(() => this.creando.set(false)))
      .subscribe((reserva) => {
        this.snackBar.open(`Reserva directa ${reserva.codigo} registrada`, 'Cerrar', {
          duration: 5000,
        });
        this.router.navigate(['/recepcion/hoy']);
      });
  }

  /** Una fila por persona; el titular se propone como primer ocupante. */
  private prepararOcupantes(): void {
    const personas = this.estadia.controls.personas.value;
    if (this.ocupantes.length === personas) {
      return;
    }
    const titular = this.titular.value;
    this.ocupantes.clear();
    for (let i = 0; i < personas; i++) {
      this.ocupantes.push(
        i === 0 && titular
          ? crearOcupante(titular.nombre, desdeFechaIso(titular.fechaNacimiento))
          : crearOcupante(),
      );
    }
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

  private solicitud(): CrearReservaRequest | null {
    const { entrada, salida, apartamento } = this.estadia.getRawValue();
    const titular = this.titular.value;
    if (!entrada || !salida || !apartamento || !titular || this.ocupantes.invalid) {
      return null;
    }
    return {
      codigoApartamento: apartamento,
      fechaEntrada: aFechaIso(entrada),
      fechaSalida: aFechaIso(salida),
      documentoTitular: titular.documento,
      ocupantes: aOcupantes(this.ocupantes),
    };
  }
}