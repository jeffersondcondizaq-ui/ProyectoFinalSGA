import { Canal, EstadoReserva, TipoTemporada } from './enums';
import { Fecha, Ocupante } from './persona.model';

export interface DetalleNoche {
  fecha: Fecha;
  temporada: TipoTemporada;
  tarifaPersona: number;
  pagadores: number;
  subtotal: number;
}

export interface Cotizacion {
  codigoApartamento: string;
  fechaEntrada: Fecha;
  fechaSalida: Fecha;
  detalleNoches: DetalleNoche[];
  valorTotal: number;
  anticipo?: number;
}

export interface Reserva {
  codigo: string;
  canal: Canal;
  estado: EstadoReserva;
  documentoTitular: string;
  nombreTitular: string;
  codigoApartamento: string;
  fechaEntrada: Fecha;
  fechaSalida: Fecha;
  fechaCreacion: string;
  ocupantes: Ocupante[];
  valorEstancia: number;
  saldo: number;
  codigoExterno?: string;
  plataforma?: string;
}

export interface CotizarRequest {
  codigoApartamento: string;
  fechaEntrada: Fecha;
  fechaSalida: Fecha;
  ocupantes: Ocupante[];
}

export interface CrearReservaRequest extends CotizarRequest {
  documentoTitular?: string;
}

export interface ReservaExternaRequest extends CrearReservaRequest {
  codigoExterno: string;
  plataforma: string;
}

export interface ConflictoCanal {
  codigoExterno: string;
  fechaRegistro: string;
  codigoReservaExistente: string;
  descripcion: string;
}
