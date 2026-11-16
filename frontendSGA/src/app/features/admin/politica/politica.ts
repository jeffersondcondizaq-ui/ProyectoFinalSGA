import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ConfiguracionApi } from '../../../core/api/configuracion.api';

/** Las horas de retención total deben ser menos que los días de devolución total. */
function horasMenoresQueDias(grupo: AbstractControl): ValidationErrors | null {
  const { diasDevolucionTotal, horasRetencionTotal } = grupo.value;
  return horasRetencionTotal >= diasDevolucionTotal * 24 ? { rangoInvalido: true } : null;
}

@Component({
  selector: 'app-politica',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './politica.html',
  styleUrl: './politica.scss',
})
export class Politica {
  private readonly configuracionApi = inject(ConfiguracionApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly cargada = signal(false);
  protected readonly guardando = signal(false);

  protected readonly form = new FormGroup(
    {
      diasDevolucionTotal: new FormControl(7, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(1)],
      }),
      horasRetencionTotal: new FormControl(48, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(1)],
      }),
      porcentajeRetencionParcial: new FormControl(50, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(0), Validators.max(100)],
      }),
    },
    { validators: horasMenoresQueDias },
  );

  /** Valores actuales del formulario, para el resumen en texto que se ve a la derecha. */
  protected readonly valores = toSignal(this.form.valueChanges, {
    initialValue: this.form.getRawValue(),
  });

  constructor() {
    this.configuracionApi.obtenerPolitica().subscribe((politica) => {
      this.form.setValue(politica);
      this.cargada.set(true);
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.configuracionApi
      .configurarPolitica(this.form.getRawValue())
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((politica) => {
        this.form.setValue(politica);
        this.form.markAsPristine();
        this.snackBar.open('Política de cancelación actualizada', 'Cerrar', { duration: 3000 });
      });
  }
}
