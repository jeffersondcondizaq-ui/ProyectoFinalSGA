import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { HuespedApi } from '../../../core/api/huesped.api';
import { Huesped } from '../../../shared/models';
import { desdeFechaIso } from '../../../shared/utils/fechas';

/** Tamaño máximo de la foto (se envía como texto base64 dentro del JSON). */
const MAX_FOTO_BYTES = 500 * 1024;

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class Perfil {
  private readonly huespedApi = inject(HuespedApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly perfil = signal<Huesped | null>(null);
  protected readonly editando = signal(false);
  protected readonly guardando = signal(false);
  /** Foto elegida mientras se edita (aún no guardada). */
  protected readonly fotoNueva = signal<string | undefined>(undefined);

  /** Ej.: '18 de julio de 1995'. */
  protected readonly fechaNacimiento = computed(() => {
    const p = this.perfil();
    return p ? desdeFechaIso(p.fechaNacimiento).toLocaleDateString('es-CO', { dateStyle: 'long' }) : '';
  });

  protected readonly fotoMostrada = computed(() =>
    this.editando() ? this.fotoNueva() : this.perfil()?.foto,
  );

  protected readonly form = new FormGroup({
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{10}$/)],
    }),
    correo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  constructor() {
    this.huespedApi.miPerfil().subscribe((perfil) => this.perfil.set(perfil));
  }

  protected editar(): void {
    const p = this.perfil();
    if (!p) {
      return;
    }
    this.form.reset({ telefono: p.telefono, correo: p.correo });
    this.fotoNueva.set(p.foto);
    this.editando.set(true);
  }

  protected cancelar(): void {
    this.editando.set(false);
  }

  protected elegirFoto(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    input.value = ''; // permite volver a elegir el mismo archivo
    if (!archivo) {
      return;
    }
    if (!archivo.type.startsWith('image/')) {
      this.snackBar.open('El archivo debe ser una imagen', 'Cerrar', { duration: 4000 });
      return;
    }
    if (archivo.size > MAX_FOTO_BYTES) {
      this.snackBar.open('La foto no puede pesar más de 500 KB', 'Cerrar', { duration: 4000 });
      return;
    }
    const lector = new FileReader();
    lector.onload = () => this.fotoNueva.set(lector.result as string);
    lector.readAsDataURL(archivo);
  }

  protected quitarFoto(): void {
    this.fotoNueva.set(undefined);
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.huespedApi
      .actualizarPerfil({ ...this.form.getRawValue(), foto: this.fotoNueva() })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((perfil) => {
        this.perfil.set(perfil);
        this.editando.set(false);
        this.snackBar.open('Perfil actualizado', 'Cerrar', { duration: 4000 });
      });
  }
}