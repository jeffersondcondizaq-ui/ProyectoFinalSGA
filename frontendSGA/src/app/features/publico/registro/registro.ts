import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AutenticacionApi } from '../../../core/api/autenticacion.api';

/** Convierte una fecha del datepicker a 'YYYY-MM-DD' (LocalDate en el backend). */
function aFechaIso(fecha: Date): string {
  const mm = String(fecha.getMonth() + 1).padStart(2, '0');
  const dd = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mm}-${dd}`;
}

/** Valida que 'contrasena' y 'confirmacion' sean iguales (se pone en el FormGroup). */
const contrasenasIguales: ValidatorFn = (grupo: AbstractControl): ValidationErrors | null => {
  const contrasena = grupo.get('contrasena')?.value;
  const confirmacion = grupo.get('confirmacion');
  if (!confirmacion) {
    return null;
  }
  const iguales = contrasena === confirmacion.value;
  // El error se marca en 'confirmacion' para que se vea debajo de ese campo.
  if (!iguales && confirmacion.value) {
    confirmacion.setErrors({ ...confirmacion.errors, noCoincide: true });
  } else if (confirmacion.hasError('noCoincide')) {
    const { noCoincide, ...resto } = confirmacion.errors ?? {};
    confirmacion.setErrors(Object.keys(resto).length ? resto : null);
  }
  return null;
};

@Component({
  selector: 'app-registro',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro {
  private readonly auth = inject(AutenticacionApi);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  /** La fecha de nacimiento no puede ser futura. */
  protected readonly hoy = new Date();
  protected readonly enviando = signal(false);
  protected readonly verContrasena = signal(false);

  protected readonly form = new FormGroup(
    {
      documento: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/^\d{6,10}$/)],
      }),
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
      usuario: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/^[a-zA-Z0-9._]{4,20}$/)],
      }),
      contrasena: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(6)],
      }),
      confirmacion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: contrasenasIguales },
  );

  protected registrar(): void {
    const { confirmacion, fechaNacimiento, ...datos } = this.form.getRawValue();
    if (this.form.invalid || !fechaNacimiento) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.auth
      .registrarHuesped({ ...datos, nombre: datos.nombre.trim(), fechaNacimiento: aFechaIso(fechaNacimiento) })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: () => {
          this.snackBar.open('Cuenta creada. Ya puedes iniciar sesión.', 'Cerrar', { duration: 5000 });
          this.router.navigate(['/login']);
        },
        // El mensaje de error (p. ej. usuario repetido) lo muestra el errorInterceptor.
        error: () => {},
      });
  }
}