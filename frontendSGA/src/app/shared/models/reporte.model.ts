import { Canal } from './enums';

export interface OcupacionApartamento {
  codigoApartamento: string;
  nombre: string;
  nochesOcupadas: number;
  porcentaje: number;
}

export interface IngresosPeriodo {
  desde: string;
  hasta: string;
  total: number;
}

export interface ReservasPorCanal {
  canal: Canal;
  total: number;
  canceladas: number;
  noShow: number;
}

export interface NovedadesApartamento {
  codigoApartamento: string;
  nombre: string;
  cantidad: number;
}
