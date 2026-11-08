import { Rol } from '../shared/models';

export interface OpcionMenu {
  etiqueta: string;
  icono: string;
  ruta: string;
}

/** Opciones del menú lateral según el rol de la sesión. */
export const MENU_POR_ROL: Record<Rol, OpcionMenu[]> = {
  HUESPED: [
    { etiqueta: 'Buscar apartamentos', icono: 'search', ruta: '/huesped/disponibilidad' },
    { etiqueta: 'Mis reservas', icono: 'event_note', ruta: '/huesped/reservas' },
    { etiqueta: 'Mi perfil', icono: 'person', ruta: '/huesped/perfil' },
  ],
  RECEPCIONISTA: [
    { etiqueta: 'Operación del día', icono: 'today', ruta: '/recepcion/hoy' },
    { etiqueta: 'Reserva directa', icono: 'add_call', ruta: '/recepcion/reserva-directa' },
    { etiqueta: 'Reserva externa', icono: 'travel_explore', ruta: '/recepcion/reserva-externa' },
    { etiqueta: 'Conflictos de canal', icono: 'sync_problem', ruta: '/recepcion/conflictos' },
    { etiqueta: 'Estado de apartamentos', icono: 'cleaning_services', ruta: '/recepcion/apartamentos' },
  ],
  ADMINISTRADOR: [
    { etiqueta: 'Apartamentos', icono: 'apartment', ruta: '/admin/apartamentos' },
    { etiqueta: 'Temporadas', icono: 'date_range', ruta: '/admin/temporadas' },
    { etiqueta: 'Política de cancelación', icono: 'policy', ruta: '/admin/politica' },
    { etiqueta: 'Empleados', icono: 'badge', ruta: '/admin/empleados' },
    { etiqueta: 'Reportes', icono: 'bar_chart', ruta: '/admin/reportes' },
  ],
};

export const MENU_PUBLICO: OpcionMenu[] = [
  { etiqueta: 'Inicio', icono: 'home', ruta: '/inicio' },
  { etiqueta: 'Iniciar sesión', icono: 'login', ruta: '/login' },
  { etiqueta: 'Crear cuenta', icono: 'person_add', ruta: '/registro' },
];
