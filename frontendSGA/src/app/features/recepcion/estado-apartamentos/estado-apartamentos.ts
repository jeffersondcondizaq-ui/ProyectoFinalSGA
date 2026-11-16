import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ApartamentoApi } from '../../../core/api/apartamento.api';
import {
  ETIQUETA_ESTADO_APARTAMENTO,
  EstadoApartamentoChip,
} from '../../../shared/components/estado-apartamento/estado-apartamento';
import { Apartamento, EstadoApartamento, TipoNovedad } from '../../../shared/models';

/**
 * Estados que la recepción cambia a mano. OCUPADO no está: lo pone el check-in,
 * y PENDIENTE_PREPARACION lo pone el check-out (aunque también se puede marcar a mano).
 */
const ESTADOS_MANUALES: EstadoApartamento[] = [
  'PENDIENTE_PREPARACION',
  'EN_PREPARACION',
  'PREPARADO',
  'FUERA_DE_SERVICIO',
];

const TIPOS_NOVEDAD: { valor: TipoNovedad; etiqueta: string }[] = [
  { valor: 'DANIO', etiqueta: 'Daño' },
  { valor: 'FALTANTE', etiqueta: 'Faltante' },
  { valor: 'OTRA', etiqueta: 'Otra situación' },
];

type Edicion = { codigo: string; modo: 'estado' | 'novedad' };

@Component({
  selector: 'app-estado-apartamentos',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    EstadoApartamentoChip,
  ],
  templateUrl: './estado-apartamentos.html',
  styleUrl: './estado-apartamentos.scss',
})
export class EstadoApartamentos {
  private readonly apartamentoApi = inject(ApartamentoApi);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly etiquetas = ETIQUETA_ESTADO_APARTAMENTO;
  protected readonly tiposNovedad = TIPOS_NOVEDAD;

  protected readonly apartamentos = signal<Apartamento[] | null>(null);
  /** Solo se edita un apartamento a la vez (estado o novedad). */
  protected readonly edicion = signal<Edicion | null>(null);
  protected readonly guardando = signal(false);

  protected readonly estadoForm = new FormGroup({
    estado: new FormControl<EstadoApartamento | null>(null, Validators.required),
    motivo: new FormControl('', { nonNullable: true }),
  });
  protected readonly novedadForm = new FormGroup({
    tipo: new FormControl<TipoNovedad>('DANIO', { nonNullable: true }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    // El motivo solo es obligatorio cuando el apartamento queda fuera de servicio.
    this.estadoForm.controls.estado.valueChanges.subscribe((estado) => {
      const motivo = this.estadoForm.controls.motivo;
      motivo.setValidators(estado === 'FUERA_DE_SERVICIO' ? Validators.required : null);
      motivo.updateValueAndValidity();
    });
    this.apartamentoApi
      .listar()
      .subscribe((lista) => this.apartamentos.set(lista.filter((a) => a.activo)));
  }

  /** Estados a los que se puede pasar desde el actual (el backend valida la transición). */
  protected opciones(actual: EstadoApartamento): EstadoApartamento[] {
    return ESTADOS_MANUALES.filter((e) => e !== actual);
  }

  protected editar(apartamento: Apartamento, modo: Edicion['modo']): void {
    this.estadoForm.reset({ estado: null, motivo: '' });
    this.novedadForm.reset({ tipo: 'DANIO', descripcion: '' });
    this.edicion.set({ codigo: apartamento.codigo, modo });
  }

  protected guardarEstado(apartamento: Apartamento): void {
    const { estado, motivo } = this.estadoForm.getRawValue();
    if (this.estadoForm.invalid || !estado) {
      this.estadoForm.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.apartamentoApi
      .cambiarEstado(apartamento.codigo, estado, motivo.trim() || undefined)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe((actualizado) => {
        this.apartamentos.update((lista) =>
          (lista ?? []).map((a) => (a.codigo === actualizado.codigo ? actualizado : a)),
        );
        this.edicion.set(null);
        this.snackBar.open(`${apartamento.codigo}: ${this.etiquetas[estado]}`, 'Cerrar', {
          duration: 3000,
        });
      });
  }

  protected guardarNovedad(apartamento: Apartamento): void {
    if (this.novedadForm.invalid) {
      this.novedadForm.markAllAsTouched();
      return;
    }
    const { tipo, descripcion } = this.novedadForm.getRawValue();
    this.guardando.set(true);
    this.apartamentoApi
      .registrarNovedad(apartamento.codigo, { tipo, descripcion: descripcion.trim() })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe(() => {
        this.edicion.set(null);
        this.snackBar.open(`Novedad registrada en ${apartamento.codigo}`, 'Cerrar', {
          duration: 3000,
        });
      });
  }
}
