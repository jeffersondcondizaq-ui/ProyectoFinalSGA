import { MedioPago, TipoCargo } from './enums';

export type TipoMovimiento = 'CARGO' | 'PAGO' | 'DEVOLUCION';

export interface MovimientoFolio {
  id: string;
  tipo: TipoMovimiento;
  fecha: string;
  valor: number;
  descripcion: string;
  esCorreccion: boolean;
  tipoCargo?: TipoCargo;
  medio?: MedioPago;
}

export interface Folio {
  codigoReserva: string;
  movimientos: MovimientoFolio[];
  totalCargos: number;
  totalPagos: number;
  saldo: number;
}

export interface PagoRequest {
  medio: MedioPago;
  valor: number;
  descripcion: string;
}

export interface CargoRequest {
  tipo: TipoCargo;
  valor: number;
  descripcion: string;
}
