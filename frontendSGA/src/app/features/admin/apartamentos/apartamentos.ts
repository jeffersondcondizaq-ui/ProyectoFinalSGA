import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { finalize } from 'rxjs';
import { ApartamentoApi } from '../../../core/api/apartamento.api';
import { EstadoApartamentoChip } from '../../../shared/components/estado-apartamento/estado-apartamento';
import { Apartamento, Tarifa, TipoTemporada } from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';
import { aFechaIso } from '../../../shared/utils/fechas';

/** Qué se está haciendo en el panel de arriba de la tabla. */
type Panel =
  { modo: 'nuevo' } | { modo: 'editar' | 'bloqueo' | 'eliminar'; apartamento: Apartamento };

@Component({
  selector: 'app-apartamentos',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSlideToggleModule,
    MatTableModule,
    MatTooltipModule,
    EstadoApartamentoChip,
    CopPipe,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './apartamentos.html',
  styleUrl: './apartamentos.scss',
})
export class Apartamentos {
  private readonly apartamentoApi = inject(ApartamentoApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columnas = ['codigo', 'nombre', 'capacidad', 'tarifas', 'estado', 'acciones'];
  protected readonly hoy = new Date();

  protected readonly apartamentos = signal<Apartamento[] | null>(null);
  protected readonly mostrarEliminados = signal(false);
  protected readonly visibles = computed(() =>
    (this.apartamentos() ?? []).filter((a) => a.activo || this.mostrarEliminados()),
  );

  protected readonly panel = signal<Panel | null>(null);
  protected readonly guardando = signal(false);
  /** Características del formulario (chips). */
  protected readonly caracteristicas = signal<string[]>([]);

  protected readonly form = new FormGroup({
    codigo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^APT-\d{3}$/)],
    }),
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    numDormitorios: new FormControl(1, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
    capacidadMaxima: new FormControl(2, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
    baja: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    media: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    alta: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
  });

  protected readonly bloqueo = new FormGroup({
    fechaInicio: new FormControl<Date | null>(null, Validators.required),
    fechaFin: new FormControl<Date | null>(null, Validators.required),
    motivo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.apartamentoApi.listar().subscribe((lista) => this.apartamentos.set(lista));
  }

  /** Valor por persona por noche de una temporada (para la tabla y el formulario). */
  protected tarifa(apartamento: Apartamento, tipo: TipoTemporada): number | null {
    return apartamento.tarifas.find((t) => t.tipoTemporada === tipo)?.valorPersonaNoche ?? null;
  }

  protected nuevo(): void {
    this.form.reset();
    this.form.controls.codigo.enable();
    this.caracteristicas.set([]);
    this.panel.set({ modo: 'nuevo' });
  }

  protected editar(apartamento: Apartamento): void {
    this.form.reset({
      codigo: apartamento.codigo,
      nombre: apartamento.nombre,
      numDormitorios: apartamento.numDormitorios,
      capacidadMaxima: apartamento.capacidadMaxima,
      baja: this.tarifa(apartamento, 'BAJA'),
      media: this.tarifa(apartamento, 'MEDIA'),
      alta: this.tarifa(apartamento, 'ALTA'),
    });
    // El código es la identidad del apartamento: no se cambia al editar.
    this.form.controls.codigo.disable();
    this.caracteristicas.set([...apartamento.caracteristicas]);
    this.panel.set({ modo: 'editar', apartamento });
  }

  protected bloquear(apartamento: Apartamento): void {
    this.bloqueo.reset({ fechaInicio: null, fechaFin: null, motivo: '' });
    this.panel.set({ modo: 'bloqueo', apartamento });
  }

  protected agregarCaracteristica(evento: MatChipInputEvent): void {
    const valor = evento.value.trim();
    if (valor && !this.caracteristicas().includes(valor)) {
      this.caracteristicas.update((lista) => [...lista, valor]);
    }
    evento.chipInput.clear();
  }

  protected quitarCaracteristica(valor: string): void {
    this.caracteristicas.update((lista) => lista.filter((c) => c !== valor));
  }

  protected guardar(): void {
    const panel = this.panel();
    if (!panel || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { codigo, nombre, numDormitorios, capacidadMaxima, baja, media, alta } =
      this.form.getRawValue();
    const tarifas: Tarifa[] = [
      { tipoTemporada: 'BAJA', valorPersonaNoche: baja! },
      { tipoTemporada: 'MEDIA', valorPersonaNoche: media! },
      { tipoTemporada: 'ALTA', valorPersonaNoche: alta! },
    ];
    const datos = {
      nombre: nombre.trim(),
      numDormitorios,
      capacidadMaxima,
      caracteristicas: this.caracteristicas(),
      tarifas,
    };
    const peticion =
      panel.modo === 'nuevo'
        ? this.apartamentoApi.crear({ codigo, ...datos })
        : this.apartamentoApi.actualizar(codigo, datos);

    this.guardando.set(true);
    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe((guardado) => {
      this.reemplazar(guardado, panel.modo === 'nuevo');
      this.panel.set(null);
      this.snackBar.open(`Apartamento ${guardado.codigo} guardado`, 'Cerrar', { duration: 3000 });
    });
  }

  protected guardarBloqueo(apartamento: Apartamento): void {
    const { fechaInicio, fechaFin, motivo } = this.bloqueo.getRawValue();
    if (this.bloqueo.invalid || !fechaInicio || !fechaFin) {
      this.bloqueo.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.apartamentoApi
      .registrarBloqueo(apartamento.codigo, {
        fechaInicio: aFechaIso(fechaInicio),
        fechaFin: aFechaIso(fechaFin),
        motivo: motivo.trim(),
      })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((actualizado) => {
        this.reemplazar(actualizado);
        // Se deja el panel abierto para que se vea el bloqueo agregado en la lista.
        this.panel.set({ modo: 'bloqueo', apartamento: actualizado });
        this.bloqueo.reset({ fechaInicio: null, fechaFin: null, motivo: '' });
        this.snackBar.open('Bloqueo registrado', 'Cerrar', { duration: 3000 });
      });
  }

  protected eliminar(apartamento: Apartamento): void {
    this.guardando.set(true);
    this.apartamentoApi
      .eliminar(apartamento.codigo)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe(() => {
        // Eliminación lógica: se conserva en la lista como inactivo.
        this.reemplazar({ ...apartamento, activo: false });
        this.panel.set(null);
        this.snackBar.open(`Apartamento ${apartamento.codigo} eliminado`, 'Cerrar', {
          duration: 3000,
        });
      });
  }

  private reemplazar(apartamento: Apartamento, esNuevo = false): void {
    this.apartamentos.update((lista) =>
      esNuevo
        ? [...(lista ?? []), apartamento]
        : (lista ?? []).map((a) => (a.codigo === apartamento.codigo ? apartamento : a)),
    );
  }
}
