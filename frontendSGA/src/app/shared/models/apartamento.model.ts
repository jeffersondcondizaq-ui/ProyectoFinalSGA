import { EstadoApartamento, TipoNovedad, TipoTemporada } from './enums';
import { Fecha } from './persona.model';

export interface Tarifa {
  tipoTemporada: TipoTemporada;
  valorPersonaNoche: number;
}

export interface Bloqueo {
  fechaInicio: Fecha;
  fechaFin: Fecha;
  motivo: string;
}

export interface Novedad {
  fecha: string;
  tipo: TipoNovedad;
  descripcion: string;
  reportadaPor?: string;
}

export type NovedadRequest = Pick<Novedad, 'tipo' | 'descripcion'>;

export interface Apartamento {
  codigo: string;
  nombre: string;
  numDormitorios: number;
  capacidadMaxima: number;
  caracteristicas: string[];
  estado: EstadoApartamento;
  motivoFueraServicio?: string;
  activo: boolean;
  tarifas: Tarifa[];
  bloqueos?: Bloqueo[];
  fotos?: string[];
}

export type ApartamentoRequest = Pick<
  Apartamento,
  'codigo' | 'nombre' | 'numDormitorios' | 'capacidadMaxima' | 'caracteristicas' | 'tarifas'
>;
