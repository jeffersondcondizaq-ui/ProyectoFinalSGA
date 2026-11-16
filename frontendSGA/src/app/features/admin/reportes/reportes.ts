import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { finalize, forkJoin } from 'rxjs';
import { ReporteApi } from '../../../core/api/reporte.api';
import {
  Canal,
  IngresosPeriodo,
  NovedadesApartamento,
  OcupacionApartamento,
  Reserva,
  ReservasPorCanal,
} from '../../../shared/models';
import { CopPipe } from '../../../shared/pipes/cop.pipe';
import { aFechaIso } from '../../../shared/utils/fechas';

const ETIQUETA_CANAL: Record<Canal, string> = {
  PORTAL: 'Portal',
  DIRECTA: 'Directa',
  EXTERNA: 'Externa',
};

/** Todo lo que muestra la pantalla; cada parte la calcula el backend. */
interface Datos {
  ocupacion: OcupacionApartamento[];
  ingresos: IngresosPeriodo;
  canales: ReservasPorCanal[];
  saldos: Reserva[];
  novedades: NovedadesApartamento[];
}

@Component({
  selector: 'app-reportes',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    CopPipe,
  ],
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'es-CO' }],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class Reportes {
  private readonly reporteApi = inject(ReporteApi);

  protected readonly columnasCanal = ['canal', 'total', 'canceladas', 'noShow'];
  protected readonly columnasSaldos = ['codigo', 'titular', 'apartamento', 'salida', 'saldo'];

  /** Por defecto, el mes actual completo. */
  protected readonly filtro = new FormGroup({
    desde: new FormControl<Date | null>(
      new Date(new Date().getFullYear(), new Date().getMonth(), 1),
      Validators.required,
    ),
    hasta: new FormControl<Date | null>(
      new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0),
      Validators.required,
    ),
  });

  protected readonly datos = signal<Datos | null>(null);
  protected readonly cargando = signal(false);

  // Solo sumas para mostrar totales en las tarjetas de arriba; no hay reglas de negocio aquí.
  protected readonly totalReservas = computed(() =>
    (this.datos()?.canales ?? []).reduce((suma, c) => suma + c.total, 0),
  );
  protected readonly totalSaldos = computed(() =>
    (this.datos()?.saldos ?? []).reduce((suma, r) => suma + r.saldo, 0),
  );
  /** La barra más larga de novedades es la del apartamento con más. */
  protected readonly maxNovedades = computed(() =>
    Math.max(1, ...(this.datos()?.novedades ?? []).map((n) => n.cantidad)),
  );

  constructor() {
    this.consultar();
  }

  protected canal(canal: Canal): string {
    return ETIQUETA_CANAL[canal];
  }

  protected consultar(): void {
    const { desde, hasta } = this.filtro.getRawValue();
    if (this.filtro.invalid || !desde || !hasta) {
      this.filtro.markAllAsTouched();
      return;
    }
    const [d, h] = [aFechaIso(desde), aFechaIso(hasta)];
    this.cargando.set(true);
    // forkJoin espera las cinco consultas y las entrega juntas.
    forkJoin({
      ocupacion: this.reporteApi.ocupacion(d, h),
      ingresos: this.reporteApi.ingresos(d, h),
      canales: this.reporteApi.reservasPorCanal(d, h),
      saldos: this.reporteApi.saldosPendientes(),
      novedades: this.reporteApi.novedades(),
    })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe((datos) => this.datos.set(datos));
  }
}
