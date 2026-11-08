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
