// Mismos valores que los enums del dominio en el backend.

export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'ADMINISTRADOR';

export type EstadoApartamento =
  | 'PREPARADO'
  | 'OCUPADO'
  | 'PENDIENTE_PREPARACION'
  | 'EN_PREPARACION'
  | 'FUERA_DE_SERVICIO';

export type TipoTemporada = 'BAJA' | 'MEDIA' | 'ALTA';

export type TipoNovedad = 'DANIO' | 'FALTANTE' | 'OTRA';

export type EstadoReserva =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'EN_CURSO'
  | 'FINALIZADA'
  | 'CANCELADA'
  | 'NO_SHOW';

export type Canal = 'PORTAL' | 'DIRECTA' | 'EXTERNA';

export type TipoCargo = 'ALOJAMIENTO' | 'SERVICIO_ADICIONAL' | 'PENALIDAD';

export type MedioPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';
