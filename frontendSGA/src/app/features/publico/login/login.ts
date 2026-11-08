import { Component, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AutenticacionApi } from '../../../core/api/autenticacion.api';
import { SesionService } from '../../../core/auth/sesion.service';
import { Rol } from '../../../shared/models';

const INICIO_POR_ROL: Record<Rol, string> = {
  HUESPED: '/huesped',
  RECEPCIONISTA: '/recepcion',
  ADMINISTRADOR: '/admin',
};

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AutenticacionApi);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  /** Query params que llegan desde la búsqueda de la página principal (opcionales). */
  readonly entrada = input<string>();
  readonly salida = input<string>();
  readonly personas = input<string>();

  protected readonly usarMocks = environment.usarMocks;
  protected readonly enviando = signal(false);
  protected readonly verContrasena = signal(false);

  protected readonly form = new FormGroup({
    usuario: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  protected ingresar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.auth
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (sesion) => {
          this.sesion.iniciar(sesion);
          this.redirigir(sesion.rol);
        },
        // El mensaje de error ya lo muestra el errorInterceptor.
        error: () => {},
      });
  }

  /** Si el huésped venía de buscar en la página principal, lo lleva directo a Disponibilidad. */
  private redirigir(rol: Rol): void {
    if (rol === 'HUESPED' && this.entrada() && this.salida()) {
      this.router.navigate(['/huesped/disponibilidad'], {
        queryParams: { entrada: this.entrada(), salida: this.salida(), personas: this.personas() },
      });
      return;
    }
    this.router.navigateByUrl(INICIO_POR_ROL[rol]);
  }
}