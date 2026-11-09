import { Rol } from './enums';

/** Fechas como 'YYYY-MM-DD' (LocalDate en el backend). */
export type Fecha = string;

export interface Ocupante {
  nombre: string;
  fechaNacimiento: Fecha;
}

export interface Huesped {
  documento: string;
  nombre: string;
  fechaNacimiento: Fecha;
  edad: number;
  telefono: string;
  correo: string;
  usuario: string;
  foto?: string;
}
export type NuevoHuespedRequest = Omit<Huesped, 'edad' | 'usuario' | 'foto'>;

export interface RegistroHuespedRequest extends Omit<Huesped, 'edad'> {
  contrasena: string;
}

export interface Empleado {
  documento: string;
  nombre: string;
  fechaNacimiento: Fecha;
  telefono: string;
  correo: string;
  usuario: string;
  rol: Rol;
  fechaContratacion: Fecha;
  activo: boolean;
}

export interface LoginRequest {
  usuario: string;
  contrasena: string;
}

export interface Sesion {
  token: string;
  usuario: string;
  nombre: string;
  rol: Rol;
}
