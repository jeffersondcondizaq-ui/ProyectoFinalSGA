import { Component, input } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Ocupante } from '../../models';
import { aFechaIso } from '../../utils/fechas';

export type OcupanteForm = FormGroup<{
  nombre: FormControl<string>;
  fechaNacimiento: FormControl<Date | null>;
}>;

/** Crea la fila de un ocupante (nombre y fecha de nacimiento, ambos obligatorios). */
export function crearOcupante(nombre = '', fechaNacimiento: Date | null = null): OcupanteForm {
  return new FormGroup({
    nombre: new FormControl(nombre, { nonNullable: true, validators: [Validators.required] }),
    fechaNacimiento: new FormControl<Date | null>(fechaNacimiento, Validators.required),
  });
}

/** Convierte las filas del formulario al formato que espera el backend. */
export function aOcupantes(filas: FormArray<OcupanteForm>): Ocupante[] {
  return filas.getRawValue().map((o) => ({
    nombre: o.nombre.trim(),
    fechaNacimiento: aFechaIso(o.fechaNacimiento!),
  }));
}

/** Filas de ocupantes reutilizables (reserva Portal, Directa y Externa). */
@Component({
  selector: 'app-ocupantes-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './ocupantes-form.html',
  styleUrl: './ocupantes-form.scss',
})
export class OcupantesForm {
  readonly ocupantes = input.required<FormArray<OcupanteForm>>();
  /** true = se pueden agregar y quitar filas (si no, el número lo fija la búsqueda). */
  readonly editable = input(false);

  protected readonly hoy = new Date();

  protected agregar(): void {
    this.ocupantes().push(crearOcupante());
  }

  protected quitar(indice: number): void {
    this.ocupantes().removeAt(indice);
  }
}