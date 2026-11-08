/** Convierte una fecha del datepicker a 'YYYY-MM-DD' (LocalDate en el backend). */
export function aFechaIso(fecha: Date): string {
  const mm = String(fecha.getMonth() + 1).padStart(2, '0');
  const dd = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mm}-${dd}`;
}

/** Convierte 'YYYY-MM-DD' a Date sin que la zona horaria la corra un día. */
export function desdeFechaIso(fechaIso: string): Date {
  const [anio, mes, dia] = fechaIso.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** Edad en años cumplidos a una fecha de referencia (por defecto hoy). La edad nunca se guarda: se calcula. */
export function calcularEdad(fechaNacimiento: string, referencia: Date = new Date()): number {
  const nacimiento = desdeFechaIso(fechaNacimiento);
  let edad = referencia.getFullYear() - nacimiento.getFullYear();
  const yaCumplio =
    referencia.getMonth() > nacimiento.getMonth() ||
    (referencia.getMonth() === nacimiento.getMonth() && referencia.getDate() >= nacimiento.getDate());
  if (!yaCumplio) {
    edad--;
  }
  return edad;
}