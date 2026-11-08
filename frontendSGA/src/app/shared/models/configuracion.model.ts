import { TipoTemporada } from './enums';
import { Fecha } from './persona.model';

export interface PeriodoTemporada {
  tipo: TipoTemporada;
  fechaInicio: Fecha;
  fechaFin: Fecha;
  descripcion: string;
}

export interface PoliticaCancelacion {
  diasDevolucionTotal: number;
  horasRetencionTotal: number;
  porcentajeRetencionParcial: number;
}
