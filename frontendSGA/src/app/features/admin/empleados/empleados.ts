import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { finalize } from 'rxjs';
import { EmpleadoApi } from '../../../core/api/empleado.api';
import { Empleado } from '../../../shared/models';
import { aFechaIso } from '../../../shared/utils/fechas';

type Panel = { modo: 'contratar' } | { modo: 'editar' | 'desvincular'; empleado: Empleado };

@Component({
  selector: 'app-empleados',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatTooltipModule,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './empleados.html',
  styleUrl: './empleados.scss',
})
export class Empleados {
  private readonly empleadoApi = inject(EmpleadoApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columnas = ['nombre', 'usuario', 'rol', 'contratacion', 'estado', 'acciones'];
  protected readonly hoy = new Date();

  protected readonly empleados = signal<Empleado[] | null>(null);
  protected readonly panel = signal<Panel | null>(null);
  protected readonly guardando = signal(false);

  protected readonly contratacion = new FormGroup({
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
  });

  /** Al editar solo se cambian los datos de contacto. */
  protected readonly edicion = new FormGroup({
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
    this.empleadoApi.listar().subscribe((lista) => this.empleados.set(lista));
  }

  /** Solo se gestionan recepcionistas activos; el administrador no se edita desde aquí. */
  protected gestionable(empleado: Empleado): boolean {
    return empleado.rol === 'RECEPCIONISTA' && empleado.activo;
  }

  protected contratar(): void {
    this.contratacion.reset();
    this.panel.set({ modo: 'contratar' });
  }

  protected editar(empleado: Empleado): void {
    this.edicion.reset({ telefono: empleado.telefono, correo: empleado.correo });
    this.panel.set({ modo: 'editar', empleado });
  }

  protected guardarContratacion(): void {
    const { fechaNacimiento, ...datos } = this.contratacion.getRawValue();
    if (this.contratacion.invalid || !fechaNacimiento) {
      this.contratacion.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.empleadoApi
      .contratar({
        ...datos,
        nombre: datos.nombre.trim(),
        fechaNacimiento: aFechaIso(fechaNacimiento),
      })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((empleado) => {
        this.empleados.update((lista) => [...(lista ?? []), empleado]);
        this.panel.set(null);
        this.snackBar.open(`${empleado.nombre} contratado como recepcionista`, 'Cerrar', {
          duration: 4000,
        });
      });
  }

  protected guardarEdicion(empleado: Empleado): void {
    if (this.edicion.invalid) {
      this.edicion.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.empleadoApi
      .actualizar(empleado.documento, this.edicion.getRawValue())
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((actualizado) => {
        this.reemplazar(actualizado);
        this.panel.set(null);
        this.snackBar.open('Datos actualizados', 'Cerrar', { duration: 3000 });
      });
  }

  protected desvincular(empleado: Empleado): void {
    this.guardando.set(true);
    this.empleadoApi
      .desvincular(empleado.documento)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((actualizado) => {
        this.reemplazar(actualizado);
        this.panel.set(null);
        this.snackBar.open(`${empleado.nombre} fue desvinculado`, 'Cerrar', { duration: 4000 });
      });
  }

  private reemplazar(empleado: Empleado): void {
    this.empleados.update((lista) =>
      (lista ?? []).map((e) => (e.documento === empleado.documento ? empleado : e)),
    );
  }
}
