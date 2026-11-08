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
  fotos?: string[];
}
