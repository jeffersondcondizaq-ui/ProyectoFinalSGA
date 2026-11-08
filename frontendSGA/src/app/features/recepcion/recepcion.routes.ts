import { Routes } from '@angular/router';

export const RECEPCION_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'hoy' },
  { path: 'hoy', loadComponent: () => import('./panel-dia/panel-dia').then((m) => m.PanelDia) },
  {
    path: 'reserva-directa',
    loadComponent: () => import('./reserva-directa/reserva-directa').then((m) => m.ReservaDirecta),
  },
  {
    path: 'reserva-externa',
    loadComponent: () => import('./reserva-externa/reserva-externa').then((m) => m.ReservaExterna),
  },
  {
    path: 'conflictos',
    loadComponent: () => import('./conflictos/conflictos').then((m) => m.Conflictos),
  },
  {
    path: 'apartamentos',
    loadComponent: () =>
      import('./estado-apartamentos/estado-apartamentos').then((m) => m.EstadoApartamentos),
  },
  {
    path: 'folio/:codigoReserva',
    loadComponent: () => import('./folio/folio').then((m) => m.Folio),
  },
];
