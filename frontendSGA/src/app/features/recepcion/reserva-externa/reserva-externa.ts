import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { catchError, finalize, map, of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApartamentoApi } from '../../../core/api/apartamento.api';
import { ReservaApi } from '../../../core/api/reserva.api';
import { EstadoReservaChip } from '../../../shared/components/estado-reserva/estado-reserva';
import {
  OcupanteForm,
  OcupantesForm,
  aOcupantes,
  crearOcupante,
} from '../../../shared/components/ocupantes-form/ocupantes-form';
import { Apartamento, Reserva } from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';
import { aFechaIso } from '../../../shared/utils/fechas';

/** Plataformas simuladas (el enunciado dice que el canal externo es simulado). */
const PLATAFORMAS = ['Booking', 'Airbnb', 'Expedia'];

@Component({
  selector: 'app-reserva-externa',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    EstadoReservaChip,
    OcupantesForm,
    CopPipe,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './reserva-externa.html',
  styleUrl: './reserva-externa.scss',
})
export class ReservaExterna {
  private readonly apartamentoApi = inject(ApartamentoApi);
  private readonly reservaApi = inject(ReservaApi);

  protected readonly plataformas = PLATAFORMAS;
  protected readonly usarMocks = environment.usarMocks;
  protected readonly hoy = new Date();

  /** Apartamentos que se pueden vender. La disponibilidad la valida el backend al recibirla. */
  protected readonly apartamentos = toSignal(
    this.apartamentoApi.listar().pipe(
      map((lista) => lista.filter((a) => a.activo && a.estado !== 'FUERA_DE_SERVICIO')),
      catchError(() => of<Apartamento[]>([])),
    ),
    { initialValue: [] },
  );

  protected readonly form = new FormGroup({
    plataforma: new FormControl(PLATAFORMAS[0], { nonNullable: true }),
    codigoExterno: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)],
    }),
    codigoApartamento: new FormControl<string | null>(null, Validators.required),
    entrada: new FormControl<Date | null>(null, Validators.required),
    salida: new FormControl<Date | null>(null, Validators.required),
    documentoTitular: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{6,10}$/)],
    }),
  });
  /** El primer ocupante es el titular. */
  protected readonly ocupantes = new FormArray<OcupanteForm>([crearOcupante()]);

  protected readonly enviando = signal(false);
  protected readonly creada = signal<Reserva | null>(null);
  /** El backend la rechazó porque choca con otra reserva: quedó como conflicto de canal. */
  protected readonly conflicto = signal(false);

  protected recibir(): void {
    const { entrada, salida, codigoApartamento, ...datos } = this.form.getRawValue();
    if (this.form.invalid || this.ocupantes.invalid || !entrada || !salida || !codigoApartamento) {
      this.form.markAllAsTouched();
      this.ocupantes.markAllAsTouched();
      return;
    }
    if (salida <= entrada) {
      this.form.controls.salida.setErrors({ minimoUnaNoche: true });
      return;
    }
    this.creada.set(null);
    this.conflicto.set(false);
    this.enviando.set(true);
    this.reservaApi
      .recibirExterna({
        ...datos,
        codigoExterno: datos.codigoExterno.toUpperCase(),
        codigoApartamento,
        fechaEntrada: aFechaIso(entrada),
        fechaSalida: aFechaIso(salida),
        ocupantes: aOcupantes(this.ocupantes),
      })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (reserva) => this.creada.set(reserva),
        // El mensaje ya lo muestra el errorInterceptor; aquí solo se ofrece ir a Conflictos.
        error: (error: HttpErrorResponse) => this.conflicto.set(!!error.error?.conflicto),
      });
  }

  protected nueva(): void {
    this.creada.set(null);
    this.conflicto.set(false);
    this.form.reset();
    this.ocupantes.clear();
    this.ocupantes.push(crearOcupante());
  }
}