import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { finalize } from 'rxjs';
import { ConfiguracionApi } from '../../../core/api/configuracion.api';
import { PeriodoTemporada, TipoTemporada } from '../../../shared/models';
import { aFechaIso } from '../../../shared/utils/fechas';

@Component({
  selector: 'app-temporadas',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './temporadas.html',
  styleUrl: './temporadas.scss',
})
export class Temporadas {
  private readonly configuracionApi = inject(ConfiguracionApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columnas = ['tipo', 'desde', 'hasta', 'descripcion'];
  protected readonly periodos = signal<PeriodoTemporada[] | null>(null);
  protected readonly guardando = signal(false);

  protected readonly form = new FormGroup({
    // Baja no se define: es la temporada de toda noche que no esté en un periodo.
    tipo: new FormControl<TipoTemporada>('MEDIA', { nonNullable: true }),
    fechaInicio: new FormControl<Date | null>(null, Validators.required),
    fechaFin: new FormControl<Date | null>(null, Validators.required),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.configuracionApi
      .listarTemporadas()
      .subscribe((lista) => this.periodos.set(this.ordenar(lista)));
  }

  protected registrar(): void {
    const { tipo, fechaInicio, fechaFin, descripcion } = this.form.getRawValue();
    if (this.form.invalid || !fechaInicio || !fechaFin) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.configuracionApi
      .registrarTemporada({
        tipo,
        fechaInicio: aFechaIso(fechaInicio),
        fechaFin: aFechaIso(fechaFin),
        descripcion: descripcion.trim(),
      })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((periodo) => {
        // Si se cruza con otro periodo, el backend responde error y no llega aquí.
        this.periodos.update((lista) => this.ordenar([...(lista ?? []), periodo]));
        this.form.reset({ tipo: 'MEDIA', fechaInicio: null, fechaFin: null, descripcion: '' });
        this.snackBar.open('Periodo registrado', 'Cerrar', { duration: 3000 });
      });
  }

  private ordenar(lista: PeriodoTemporada[]): PeriodoTemporada[] {
    return [...lista].sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio));
  }
}
